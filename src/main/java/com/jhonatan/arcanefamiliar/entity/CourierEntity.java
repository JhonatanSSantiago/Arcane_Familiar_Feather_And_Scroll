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

    // Variáveis para guardar a missão atual da coruja
    private String nomeDoAlvo = null;
    private net.minecraft.core.BlockPos posicaoDestino = null;
    private boolean emMissao = false;

    // Variável que funciona como o "bico" ou as garras da coruja para segurar o pacote
    private net.minecraft.world.item.ItemStack pacoteCarregado = net.minecraft.world.item.ItemStack.EMPTY;

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
    public net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        net.minecraft.world.item.ItemStack itemNaMao = player.getItemInHand(hand);

        // 1. DOMAR A CORUJA (Com chance de falha)
        if (!this.isTame() && itemNaMao.getItem() == net.minecraft.world.item.Items.PUMPKIN_SEEDS) {
            if (!player.level().isClientSide) {
                if (!player.getAbilities().instabuild) itemNaMao.shrink(1);

                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        // 2. DAR OU PEGAR O PACOTE (Só funciona se for o dono)
        if (this.isTame() && this.isOwnedBy(player)) {

            boolean isCorrespondencia = itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get() ||
                    itemNaMao.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT.get();

            // Se a coruja está vazia e você tem uma correspondência válida -> Ela pega
            if (this.pacoteCarregado.isEmpty() && isCorrespondencia) {

                // Guarda o pacote no servidor e lê o destinatário
                if (!player.level().isClientSide) {
                    this.pacoteCarregado = itemNaMao.copy();
                    this.pacoteCarregado.setCount(1);
                    this.lerDestinatarioDaEncomenda();
                }

                // Consome o item nos DOIS lados (Cliente e Servidor) para ele sumir instantaneamente da mão!
                if (!player.getAbilities().instabuild) {
                    itemNaMao.shrink(1);
                }

                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
            // Se a coruja tem um pacote e você clica de MÃO VAZIA -> Ela devolve o item
            else if (!this.pacoteCarregado.isEmpty() && itemNaMao.isEmpty()) {
                if (!player.level().isClientSide) {
                    player.addItem(this.pacoteCarregado);
                    this.pacoteCarregado = net.minecraft.world.item.ItemStack.EMPTY;

                    // O 'true' no final envia a mensagem para a Action Bar (acima do HUD)
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: Devolvi a encomenda."), true);
                }
                return net.minecraft.world.InteractionResult.sidedSuccess(player.level().isClientSide);
            }
        }

        return super.mobInteract(player, hand);
    }

    private void lerDestinatarioDaEncomenda() {
        if (!this.pacoteCarregado.isEmpty() && this.pacoteCarregado.hasTag()) {
            net.minecraft.nbt.CompoundTag tag = this.pacoteCarregado.getTag();

            if (tag != null && tag.contains("DestinatarioNome")) {
                this.nomeDoAlvo = tag.getString("DestinatarioNome");
                this.emMissao = true;

                if (this.getOwner() instanceof net.minecraft.world.entity.player.Player dono) {
                    // O 'true' manda o texto de sucesso para cima da hotbar (HUD)
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§aMensageiro: Destinatário " + this.nomeDoAlvo + " identificado. A iniciar entrega!"), true);
                }
            } else {
                if (this.getOwner() instanceof net.minecraft.world.entity.player.Player dono) {
                    // O 'true' manda o texto de erro para cima da hotbar (HUD)
                    dono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMensageiro: Esta encomenda não tem um destinatário válido."), true);
                }
            }
        }
    }

    @Override
    protected void registerGoals() {
        // O número indica a prioridade (0 é o mais importante)

        // 0: Se cair na água, tenta nadar para não se afogar
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));

        // 1: Rotina de entrega de encomendas (tem prioridade sobre voar aleatoriamente)
        this.goalSelector.addGoal(1, new RotinaDeEntregaGoal());

        // 1: Voar aleatoriamente pelo mundo (evitando a água)
        this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, 1.0D));

        // 2: Se um jogador estiver a menos de 8 blocos, vira a cabeça para olhar para ele
        this.goalSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));

        // 3: Quando não estiver a fazer nada, olha em volta aleatoriamente
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
    }

    @Override
    protected net.minecraft.world.entity.ai.navigation.PathNavigation createNavigation(Level level) {
        // Cria um GPS específico para criaturas voadoras
        net.minecraft.world.entity.ai.navigation.FlyingPathNavigation navegador = new net.minecraft.world.entity.ai.navigation.FlyingPathNavigation(this, level);
        navegador.setCanOpenDoors(false); // Não queremos aves a abrir portas
        navegador.setCanFloat(true); // Permite flutuar na água
        navegador.setCanPassDoors(true); // Permite atravessar portas abertas
        return navegador;
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, net.minecraft.world.damagesource.DamageSource pSource) {
        return false; // As aves não levam dano de queda!
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null; // Por agora não vamos deixar que eles se reproduzam
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    // O cérebro de entregas da coruja
    class RotinaDeEntregaGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private int fase = 0;
        private int tempo = 0;
        private net.minecraft.world.entity.player.Player jogadorAlvo = null;
        private net.minecraft.world.entity.LivingEntity dono = null; // Variável para guardar o dono

        public RotinaDeEntregaGoal() {
            this.setFlags(java.util.EnumSet.of(net.minecraft.world.entity.ai.goal.Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            // Condição inicial para levantar voo
            return emMissao && nomeDoAlvo != null && !pacoteCarregado.isEmpty();
        }

        @Override
        public boolean canContinueToUse() {
            // CRÍTICO: Garante que a coruja não desiste do voo a meio da viagem de volta só porque esvaziou as garras
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
                // FASE 0: Levantar voo
                CourierEntity.this.setDeltaMovement(0, 0.15, 0);
                if (tempo > 60) {
                    fase = 1;
                }
            } else if (fase == 1) {
                // FASE 1: Procurar o jogador no servidor e teleportar
                this.jogadorAlvo = CourierEntity.this.level().players().stream()
                        .filter(p -> p.getName().getString().equals(nomeDoAlvo))
                        .findFirst().orElse(null);

                if (jogadorAlvo != null) {
                    CourierEntity.this.setPos(jogadorAlvo.getX(), jogadorAlvo.getY() + 5, jogadorAlvo.getZ());
                    fase = 2;
                } else {
                    // DESTINATÁRIO OFFLINE: Aborta a entrega, avisa o dono e salta para a viagem de regresso!
                    if (CourierEntity.this.getOwner() instanceof net.minecraft.world.entity.player.Player jogadorDono) {
                        jogadorDono.displayClientMessage(net.minecraft.network.chat.Component.literal("§cMensageiro: Não encontrei o destinatário. A regressar!"), true);
                    }
                    fase = 3;
                }
            } else if (fase == 2) {
                // FASE 2: Lançar a encomenda para o jogador
                net.minecraft.world.entity.item.ItemEntity itemDrop = new net.minecraft.world.entity.item.ItemEntity(
                        CourierEntity.this.level(),
                        jogadorAlvo.getX(),
                        jogadorAlvo.getY() + 1.5,
                        jogadorAlvo.getZ(),
                        pacoteCarregado.copy()
                );

                itemDrop.setDeltaMovement((CourierEntity.this.random.nextFloat() - 0.5) * 0.1, 0.1, (CourierEntity.this.random.nextFloat() - 0.5) * 0.1);
                CourierEntity.this.level().addFreshEntity(itemDrop);

                CourierEntity.this.pacoteCarregado = net.minecraft.world.item.ItemStack.EMPTY;
                jogadorAlvo.sendSystemMessage(net.minecraft.network.chat.Component.literal("§eMensageiro: Uma encomenda chegou para si!"));

                // Encomenda entregue com sucesso! Prepara a viagem de volta
                fase = 3;
            } else if (fase == 3) {
                // FASE 3: Encontrar o dono e teleportar de volta
                this.dono = CourierEntity.this.getOwner();

                if (this.dono != null) {
                    CourierEntity.this.setPos(dono.getX(), dono.getY() + 5, dono.getZ());
                    fase = 4;
                } else {
                    // Se, por acaso, o dono desconectou enquanto a coruja entregava a carta, ela fica no local
                    CourierEntity.this.emMissao = false;
                    CourierEntity.this.setNoGravity(false);
                }
            } else if (fase == 4) {
                // FASE 4: Descer até ao dono
                CourierEntity.this.setDeltaMovement(0, -0.1, 0);

                // Se chegar perto do dono ou tocar no chão
                if (CourierEntity.this.distanceTo(dono) < 2.0F || CourierEntity.this.onGround()) {
                    CourierEntity.this.emMissao = false; // Isto faz o canContinueToUse() dar falso, encerrando a rotina
                    CourierEntity.this.setNoGravity(false);
                }
            }
        }
    }
}