package com.jhonatan.arcanefamiliar.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// Implementamos FlyingAnimal para o jogo saber que esta entidade navega pelo ar
public class CourierEntity extends TamableAnimal implements FlyingAnimal {

    // Usamos o sistema de EntityData para sincronizar automaticamente com o cliente o item que a ave carrega
    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> PACOTE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(CourierEntity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);

    // Sincroniza a variante: 0 = Coruja, 1 = Corvo
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_VARIANT =
            net.minecraft.network.syncher.SynchedEntityData.defineId(CourierEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    // Variáveis para guardar a missão atual da ave
    private String nomeDoAlvo = null;
    private net.minecraft.core.BlockPos posicaoDestino = null;
    private boolean emMissao = false;

    public CourierEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        // Diz à entidade para usar o sistema de navegação e controlo de movimento aéreo
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 10, false);
    }

    // Define os atributos base da nossa coruja/corvo (vida, velocidade)
    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D) // 5 corações de vida
                .add(Attributes.FLYING_SPEED, 0.6D) // Velocidade de voo
                .add(Attributes.MOVEMENT_SPEED, 0.2D); // Velocidade a andar no chão
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        // Inicializa a variável PACOTE como vazia
        this.entityData.define(PACOTE, net.minecraft.world.item.ItemStack.EMPTY);
        this.entityData.define(DATA_VARIANT, 0); // 0 = Coruja por padrão
    }

    // Métodos Getters e Setters para a Variante
    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    // Permite que o sistema visual veja qual item a ave tem nas garras
    public net.minecraft.world.item.ItemStack getPacoteCarregado() {
        return this.entityData.get(PACOTE);
    }

    public void setPacoteCarregado(net.minecraft.world.item.ItemStack stack) {
        this.entityData.set(PACOTE, stack);
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack itemNaMao = player.getItemInHand(hand);

        // 1. Domação, Cura ou Crescimento com Frango Cru
        if (itemNaMao.getItem() == net.minecraft.world.item.Items.CHICKEN) {
            // Se for um filhote, usa o frango para fazê-lo crescer mais rápido!
            if (this.isBaby()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.ageUp(60, true); // Acelera o crescimento
                    this.level().broadcastEntityEvent(this, (byte) 7); // Partículas de sucesso
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // Se NÃO for domada, tenta domar
            else if (!this.isTame()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);

                    if (this.random.nextInt(3) == 0) {
                        this.tame(player);
                        this.navigation.stop();
                        this.setOrderedToSit(true);
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // Se JÁ for domada e estiver ferida, cura
            else if (this.getHealth() < this.getMaxHealth()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.heal(4.0F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        // 2. INTERAÇÕES DO DONO (Para aves já domadas e pertencentes ao jogador)
        if (this.isTame() && this.isOwnedBy(player)) {

            boolean isCorrespondencia = itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get() ||
                    itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT.get();

            // A) REPRODUÇÃO (Alimentar aves domadas com Sementes de Melão)
            if (itemNaMao.getItem() == net.minecraft.world.item.Items.MELON_SEEDS) {
                if (!this.isBaby() && !this.isInLove()) {
                    if (!player.level().isClientSide) {
                        if (!player.getAbilities().instabuild) {
                            itemNaMao.shrink(1);
                        }
                        this.setInLove(player);
                        this.level().broadcastEntityEvent(this, (byte) 18); // Partículas de coração
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }

            // B) Entregar a encomenda para a ave
            if (this.getPacoteCarregado().isEmpty() && isCorrespondencia) {
                if (!player.level().isClientSide) {
                    net.minecraft.world.item.ItemStack copia = itemNaMao.copy();
                    copia.setCount(1);
                    this.setPacoteCarregado(copia);
                    this.lerDestinatarioDaEncomenda();

                    // Se estava sentada, levanta automaticamente para ir fazer a entrega
                    this.setOrderedToSit(false);
                }
                if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // C) Pegar a encomenda de volta (Exige ave segurando pacote e mão vazia)
            else if (!this.getPacoteCarregado().isEmpty() && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    player.addItem(this.getPacoteCarregado());
                    this.setPacoteCarregado(net.minecraft.world.item.ItemStack.EMPTY);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: Devolvi a encomenda."), true);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // D) Comando de Sentar/Seguir (Exige mão VAZIA, ave vazia, sem correspondência e SEM SHIFT)
            else if (!player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    // Alterna o estado de sentar
                    this.setOrderedToSit(!this.isOrderedToSit());

                    if (this.isOrderedToSit()) {
                        this.navigation.stop(); // Para o movimento imediatamente
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: A aguardar (Sentado)."), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: A seguir-te."), true);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // E) Subir no dono (Shift + Clique com mão vazia e ave sem pacote)
            else if (player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    this.setOrderedToSit(false);
                    // Tenta pôr no ombro (igual ao papagaio)
                    if (this.setEntityOnShoulder(player)) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: Às ordens!"), true);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    private void lerDestinatarioDaEncomenda() {
        net.minecraft.world.item.ItemStack pacote = this.getPacoteCarregado();
        if (!pacote.isEmpty() && pacote.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = pacote.getTag();

            if (tag != null && tag.contains("DestinatarioNome")) {
                this.nomeDoAlvo = tag.getString("DestinatarioNome");
                this.emMissao = true;

                if (this.getOwner() instanceof net.minecraft.world.entity.player.Player dono) {
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMensageiro: Destinatário " + this.nomeDoAlvo + " identificado. A iniciar entrega!"), true);
                }
            } else {
                if (this.getOwner() instanceof net.minecraft.world.entity.player.Player dono) {
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMensageiro: Esta encomenda não tem um destinatário válido."), true);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Guardar a variante
        tag.putInt("Variant", this.getVariant());

        // Guardar o pacote
        if (!this.getPacoteCarregado().isEmpty()) {
            net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
            this.getPacoteCarregado().save(itemTag);
            tag.put("PacoteCarregado", itemTag);
        }

        // Guardar a missão
        tag.putBoolean("EmMissao", this.emMissao);
        if (this.nomeDoAlvo != null) {
            tag.putString("NomeDoAlvo", this.nomeDoAlvo);
        }
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // Carregar a variante
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }

        // Carregar o pacote
        if (tag.contains("PacoteCarregado")) {
            net.minecraft.nbt.CompoundTag itemTag = tag.getCompound("PacoteCarregado");
            this.setPacoteCarregado(net.minecraft.world.item.ItemStack.of(itemTag));
        }

        // Carregar a missão
        this.emMissao = tag.getBoolean("EmMissao");
        if (tag.contains("NomeDoAlvo")) {
            this.nomeDoAlvo = tag.getString("NomeDoAlvo");
        }
    }

    @Override
    protected void registerGoals() {
        // O número indica a prioridade (0 é o mais importante)

        // 0: Se cair na água, tenta nadar para não se afogar
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));

        // 1: Sentar (Se o dono mandar sentar, ela para tudo o resto)
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal(this));

        // 2: Acasalar (aproximar-se de outro parceiro em modo de amor)
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.BreedGoal(this, 1.0D));

        // 3: Rotina de entrega (tem prioridade sobre seguir o dono e passear)
        this.goalSelector.addGoal(4, new RotinaDeEntregaGoal());

        // 4: Seguir o dono
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));

        // 5: Seguir o jogador se ele tiver Frango Cru na mão
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.TemptGoal(
                this, 1.25D,
                net.minecraft.world.item.crafting.Ingredient.of(net.minecraft.world.item.Items.CHICKEN),
                false
        ));

        // 6: Andar livremente pelo chão quando estiver perto do dono
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));

        // 7: Voar aleatoriamente pelo mundo
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, 1.0D));

        // 8: Olhar para o jogador próximo
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));

        // 9: Olhar em volta aleatoriamente
        this.goalSelector.addGoal(10, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        net.minecraft.world.entity.ai.navigation.FlyingPathNavigation navegador = new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, level);
        navegador.setCanOpenDoors(false);
        navegador.setCanFloat(true);
        navegador.setCanPassDoors(true);
        return navegador;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, net.minecraft.world.damagesource.DamageSource pSource) {
        return false; // As aves não levam dano de queda!
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal == this) return false;
        if (!(otherAnimal instanceof CourierEntity otherCourier)) return false;
        // Só acasalam se forem estritamente da mesma variante/espécie (ex: coruja com coruja)
        return this.getVariant() == otherCourier.getVariant() && super.canMate(otherAnimal);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        // Define o item do ovo com base na variante da ave (0 = Coruja, 1 = Corvo, etc.)
        net.minecraft.world.item.Item eggItem = (this.getVariant() == 1)
                ? com.jhonatan.arcanefamiliar.item.ModItems.OWL_EGG.get() // Substituir por CROW_EGG quando criado
                : com.jhonatan.arcanefamiliar.item.ModItems.OWL_EGG.get();

        net.minecraft.world.item.ItemStack eggStack = new net.minecraft.world.item.ItemStack(eggItem);

        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                serverLevel,
                this.getX(), this.getY(), this.getZ(),
                eggStack
        );

        serverLevel.addFreshEntity(itemEntity);

        // Retorna null porque geramos o item do ovo no mundo em vez de gerar a entidade diretamente
        return null;
    }

    @Override
    public boolean checkSpawnRules(net.minecraft.world.level.LevelAccessor level, net.minecraft.world.entity.MobSpawnType spawnType) {
        return super.checkSpawnRules(level, spawnType);
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData, @Nullable net.minecraft.nbt.CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);

        // 10% de chance de surgir um filhote se for gerado naturalmente no mundo
        if (spawnType == net.minecraft.world.entity.MobSpawnType.NATURAL && this.random.nextFloat() < 0.10F) {
            this.setBaby(true);
        }

        return spawnGroupData;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    // Método que coloca a ave no ombro do jogador
    private boolean setEntityOnShoulder(net.minecraft.world.entity.player.Player player) {
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("id", this.getEncodeId());
        this.saveWithoutId(tag);

        if (player.setEntityOnShoulder(tag)) {
            this.discard();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.COURIER_AMBIENT.get();
    }

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource pDamageSource) {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.COURIER_HURT.get();
    }

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.COURIER_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

    // O cérebro de entregas da ave
    class RotinaDeEntregaGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int fase = 0;
        private int tempo = 0;
        private net.minecraft.world.entity.player.Player jogadorAlvo = null;
        private net.minecraft.world.entity.LivingEntity dono = null;

        public RotinaDeEntregaGoal() {
            this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return emMissao && nomeDoAlvo != null && !CourierEntity.this.getPacoteCarregado().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return emMissao;
        }

        @Override
        public void start() {
            this.fase = 0;
            this.tempo = 0;
            CourierEntity.this.setNoGravity(true);
        }

        @Override
        public void tick() {
            this.tempo++;

            if (fase == 0) {
                CourierEntity.this.setDeltaMovement(0, 0.15, 0);
                if (tempo > 60) {
                    fase = 1;
                }
            } else if (fase == 1) {
                this.jogadorAlvo = CourierEntity.this.level().players().stream()
                        .filter(p -> p.getName().getString().equals(nomeDoAlvo))
                        .findFirst().orElse(null);

                if (jogadorAlvo != null) {
                    CourierEntity.this.setPos(jogadorAlvo.getX(), jogadorAlvo.getY() + 5, jogadorAlvo.getZ());
                    fase = 2;
                } else {
                    if (CourierEntity.this.getOwner() instanceof net.minecraft.world.entity.player.Player jogadorDono) {
                        jogadorDono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMensageiro: Não encontrei o destinatário. A regressar!"), true);
                    }
                    fase = 3;
                }
            } else if (fase == 2) {
                net.minecraft.world.entity.item.ItemEntity itemDrop = new net.minecraft.world.entity.item.ItemEntity(
                        CourierEntity.this.level(),
                        jogadorAlvo.getX(),
                        jogadorAlvo.getY() + 1.5,
                        jogadorAlvo.getZ(),
                        CourierEntity.this.getPacoteCarregado().copy()
                );

                itemDrop.setDeltaMovement((CourierEntity.this.random.nextFloat() - 0.5) * 0.1, 0.1, (CourierEntity.this.random.nextFloat() - 0.5) * 0.1);
                CourierEntity.this.level().addFreshEntity(itemDrop);

                CourierEntity.this.setPacoteCarregado(net.minecraft.world.item.ItemStack.EMPTY);
                jogadorAlvo.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: Uma encomenda chegou para si!"));

                fase = 3;
            } else if (fase == 3) {
                this.dono = CourierEntity.this.getOwner();

                if (this.dono != null) {
                    CourierEntity.this.setPos(dono.getX(), dono.getY() + 5, dono.getZ());
                    fase = 4;
                } else {
                    CourierEntity.this.emMissao = false;
                    CourierEntity.this.setNoGravity(false);
                }
            } else if (fase == 4) {
                CourierEntity.this.setDeltaMovement(0, -0.1, 0);

                if (CourierEntity.this.distanceTo(dono) < 2.0F || CourierEntity.this.onGround()) {
                    CourierEntity.this.emMissao = false;
                    CourierEntity.this.setNoGravity(false);
                }
            }
        }
    }
}