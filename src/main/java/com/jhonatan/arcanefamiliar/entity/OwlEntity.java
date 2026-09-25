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

public class OwlEntity extends TamableAnimal implements FlyingAnimal {

    private static final net.minecraft.network.syncher.EntityDataAccessor<net.minecraft.world.item.ItemStack> PACOTE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(OwlEntity.class, net.minecraft.network.syncher.EntityDataSerializers.ITEM_STACK);

    private String nomeDoAlvo = null;
    private boolean emMissao = false;

    public OwlEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new net.minecraft.world.entity.ai.control.FlyingMoveControl(this, 10, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.FLYING_SPEED, 0.6D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PACOTE, net.minecraft.world.item.ItemStack.EMPTY);
    }

    public net.minecraft.world.item.ItemStack getPacoteCarregado() {
        return this.entityData.get(PACOTE);
    }

    public void setPacoteCarregado(net.minecraft.world.item.ItemStack stack) {
        this.entityData.set(PACOTE, stack);
    }

    @Override
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack itemNaMao = player.getItemInHand(hand);

        // Dieta exclusiva da Coruja: Frango Cru (domar/curar/crescer) | Coelho Cru (reproduzir)
        net.minecraft.world.item.Item itemDomacaoCura = net.minecraft.world.item.Items.CHICKEN;
        net.minecraft.world.item.Item itemReproducao = net.minecraft.world.item.Items.RABBIT;

        // 1. Domação, Cura ou Crescimento
        if (itemNaMao.getItem() == itemDomacaoCura) {
            if (this.isBaby()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.ageUp(60, true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            } else if (!this.isTame()) {
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
            } else if (this.getHealth() < this.getMaxHealth()) {
                if (!player.level().isClientSide) {
                    if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                    this.heal(4.0F);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        // 2. INTERAÇÕES DO DONO
        if (this.isTame() && this.isOwnedBy(player)) {

            boolean isCorrespondencia = itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get() ||
                    itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT.get();

            // A) REPRODUÇÃO
            if (itemNaMao.getItem() == itemReproducao) {
                if (!this.isBaby() && !this.isInLove()) {
                    if (!player.level().isClientSide) {
                        if (!player.getAbilities().instabuild) {
                            itemNaMao.shrink(1);
                        }
                        this.setInLove(player);
                        this.level().broadcastEntityEvent(this, (byte) 18);
                    }
                    return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }

            // B) Entregar a encomenda (A coruja aceita tanto pacotes quanto pergaminhos)
            if (this.getPacoteCarregado().isEmpty() && isCorrespondencia) {
                if (!player.level().isClientSide) {
                    net.minecraft.world.item.ItemStack copia = itemNaMao.copy();
                    copia.setCount(1);
                    this.setPacoteCarregado(copia);
                    this.lerDestinatarioDaEncomenda();
                    this.setOrderedToSit(false);
                }
                if (!player.getAbilities().instabuild) itemNaMao.shrink(1);
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // C) Pegar a encomenda de volta
            else if (!this.getPacoteCarregado().isEmpty() && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    player.addItem(this.getPacoteCarregado());
                    this.setPacoteCarregado(net.minecraft.world.item.ItemStack.EMPTY);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eCoruja: Devolvi a encomenda."), true);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // D) Comando de Sentar/Seguir
            else if (!player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    this.setOrderedToSit(!this.isOrderedToSit());

                    if (this.isOrderedToSit()) {
                        this.navigation.stop();
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eCoruja: A aguardar (Sentado)."), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eCoruja: A seguir-te."), true);
                    }
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // E) Subir no ombro
            else if (player.isShiftKeyDown() && this.getPacoteCarregado().isEmpty() && !isCorrespondencia && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    this.setOrderedToSit(false);
                    if (this.setEntityOnShoulder(player)) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eCoruja: Às ordens!"), true);
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
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§aCoruja: Destinatário " + this.nomeDoAlvo + " identificado. A iniciar entrega!"), true);
                }
            } else {
                if (this.getOwner() instanceof net.minecraft.world.entity.player.Player dono) {
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cCoruja: Esta encomenda não tem um destinatário válido."), true);
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (!this.getPacoteCarregado().isEmpty()) {
            net.minecraft.nbt.CompoundTag itemTag = new net.minecraft.nbt.CompoundTag();
            this.getPacoteCarregado().save(itemTag);
            tag.put("PacoteCarregado", itemTag);
        }
        tag.putBoolean("EmMissao", this.emMissao);
        if (this.nomeDoAlvo != null) {
            tag.putString("NomeDoAlvo", this.nomeDoAlvo);
        }
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("PacoteCarregado")) {
            net.minecraft.nbt.CompoundTag itemTag = tag.getCompound("PacoteCarregado");
            this.setPacoteCarregado(net.minecraft.world.item.ItemStack.of(itemTag));
        }
        this.emMissao = tag.getBoolean("EmMissao");
        if (tag.contains("NomeDoAlvo")) {
            this.nomeDoAlvo = tag.getString("NomeDoAlvo");
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new RotinaDeEntregaGoal());
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));

        // Seguir apenas com Frango Cru
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.TemptGoal(
                this, 1.25D,
                net.minecraft.world.item.crafting.Ingredient.of(net.minecraft.world.item.Items.CHICKEN),
                false
        ));

        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(9, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
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
        return false;
    }

    @Override
    public boolean canMate(Animal otherAnimal) {
        if (otherAnimal == this) return false;
        return otherAnimal instanceof OwlEntity && super.canMate(otherAnimal);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        net.minecraft.world.item.ItemStack eggStack = new net.minecraft.world.item.ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.OWL_EGG.get());
        net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                serverLevel, this.getX(), this.getY(), this.getZ(), eggStack
        );
        serverLevel.addFreshEntity(itemEntity);
        return null;
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData, @Nullable net.minecraft.nbt.CompoundTag tag) {
        spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        if (spawnType == net.minecraft.world.entity.MobSpawnType.NATURAL && this.random.nextFloat() < 0.10F) {
            this.setBaby(true);
        }
        return spawnGroupData;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

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

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.OWL_AMBIENT.get();
    }

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource pDamageSource) {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.OWL_HURT.get();
    }

    @Nullable
    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return com.jhonatan.arcanefamiliar.sound.ModSounds.OWL_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.5F;
    }

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
            return emMissao && nomeDoAlvo != null && !OwlEntity.this.getPacoteCarregado().isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            return emMissao;
        }

        @Override
        public void start() {
            this.fase = 0;
            this.tempo = 0;
            OwlEntity.this.setNoGravity(true);
        }

        @Override
        public void tick() {
            this.tempo++;

            if (fase == 0) {
                OwlEntity.this.setDeltaMovement(0, 0.15, 0);
                if (tempo > 60) {
                    fase = 1;
                }
            } else if (fase == 1) {
                this.jogadorAlvo = OwlEntity.this.level().players().stream()
                        .filter(p -> p.getName().getString().equals(nomeDoAlvo))
                        .findFirst().orElse(null);

                if (jogadorAlvo != null) {
                    OwlEntity.this.setPos(jogadorAlvo.getX(), jogadorAlvo.getY() + 5, jogadorAlvo.getZ());
                    fase = 2;
                } else {
                    if (OwlEntity.this.getOwner() instanceof net.minecraft.world.entity.player.Player jogadorDono) {
                        jogadorDono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cCoruja: Não encontrei o destinatário. A regressar!"), true);
                    }
                    fase = 3;
                }
            } else if (fase == 2) {
                net.minecraft.world.entity.item.ItemEntity itemDrop = new net.minecraft.world.entity.item.ItemEntity(
                        OwlEntity.this.level(),
                        jogadorAlvo.getX(),
                        jogadorAlvo.getY() + 1.5,
                        jogadorAlvo.getZ(),
                        OwlEntity.this.getPacoteCarregado().copy()
                );

                itemDrop.setDeltaMovement((OwlEntity.this.random.nextFloat() - 0.5) * 0.1, 0.1, (OwlEntity.this.random.nextFloat() - 0.5) * 0.1);
                OwlEntity.this.level().addFreshEntity(itemDrop);

                OwlEntity.this.setPacoteCarregado(net.minecraft.world.item.ItemStack.EMPTY);
                jogadorAlvo.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eCoruja: Uma encomenda chegou para si!"));

                fase = 3;
            } else if (fase == 3) {
                this.dono = OwlEntity.this.getOwner();

                if (this.dono != null) {
                    OwlEntity.this.setPos(dono.getX(), dono.getY() + 5, dono.getZ());
                    fase = 4;
                } else {
                    OwlEntity.this.emMissao = false;
                    OwlEntity.this.setNoGravity(false);
                }
            } else if (fase == 4) {
                OwlEntity.this.setDeltaMovement(0, -0.1, 0);

                if (OwlEntity.this.distanceTo(dono) < 2.0F || OwlEntity.this.onGround()) {
                    OwlEntity.this.emMissao = false;
                    OwlEntity.this.setNoGravity(false);
                }
            }
        }
    }
}