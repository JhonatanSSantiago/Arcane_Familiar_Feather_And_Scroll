package com.jhonatan.arcanefamiliar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SealParchmentC2SPacket {
    private final String messageText;
    private final String recipient; // Novo campo para o destinatário

    // Construtor usado quando criamos a mensagem no ecrã (Cliente)
    public SealParchmentC2SPacket(String messageText, String recipient) {
        this.messageText = messageText;
        this.recipient = recipient;
    }

    // Construtor usado quando o Servidor recebe a mensagem e a lê
    public SealParchmentC2SPacket(FriendlyByteBuf buf) {
        this.messageText = buf.readUtf();
        this.recipient = buf.readUtf(); // Lê o destinatário da rede
    }

    // Como empacotar a mensagem para enviar pela internet
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(messageText);
        buf.writeUtf(recipient); // Escreve o destinatário na rede
    }
    // O QUE ACONTECE QUANDO O SERVIDOR RECEBE A MENSAGEM:
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            //estamos no servidor
            ServerPlayer player = context.getSender();
            if (player != null) {
                boolean hasSeal = false;

                // 1. Procura no inventário do jogador pelo Selo de Cera
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.WAX_SEAL.get()) {
                        hasSeal = true;
                        stack.shrink(1); // Consome 1 unidade do selo
                        break;
                    }
                }

                if (hasSeal) {
                    // 2. Remove o Pergaminho aberto da mão do jogador
                    net.minecraft.world.item.ItemStack itemInHand = player.getMainHandItem();
                    if (itemInHand.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.PARCHMENT.get()) {
                        itemInHand.shrink(1);
                    }

                    // 3. Cria o Pergaminho Selado e grava o texto (NBT)
                    net.minecraft.world.item.ItemStack sealedParchment = new net.minecraft.world.item.ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT.get());
                    net.minecraft.nbt.CompoundTag nbt = sealedParchment.getOrCreateTag();
                    nbt.putString("MessageText", messageText);
                    nbt.putString("Author", player.getName().getString()); // Guarda quem escreveu
                    nbt.putString("Recipient", recipient); // Guarda o destinatário

                    // 4. Entrega o Pergaminho Selado ao jogador
                    if (!player.getInventory().add(sealedParchment)) {
                        player.drop(sealedParchment, false); // Se o inventário estiver cheio, atira para o chão
                    }

                    // 5. Toca o som de selar a carta
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                } else {
                    // Avisa o jogador que falta o selo, O valor 'true' no final é o que coloca a mensagem acima da barra de vida/fome
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cFalta um Selo de Cera no inventário!"), true);
                }
            }
        });
        return true;
    }
}