package com.jhonatan.arcanefamiliar.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SealParcelC2SPacket {
    private final String recipient;

    public SealParcelC2SPacket(String recipient) {
        this.recipient = recipient;
    }

    public SealParcelC2SPacket(FriendlyByteBuf buf) {
        this.recipient = buf.readUtf();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(recipient);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // 1. Verifica se o jogador tem Fio (String) no inventário
                boolean hasString = false;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (player.getInventory().getItem(i).getItem() == Items.STRING) {
                        hasString = true;
                        player.getInventory().getItem(i).shrink(1); // Consome 1 fio
                        break;
                    }
                }

                if (hasString) {
                    // Força o fecho do menu no servidor para garantir que o item é guardado no NBT
                    player.closeContainer();

                    ItemStack parcel = player.getMainHandItem();
                    if (parcel.getItem() != com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get()) {
                        parcel = player.getOffhandItem();
                    }

                    if (parcel.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get()) {
                        // 2. Cria o Pacote Selado
                        ItemStack sealedParcel = new ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get());

                        // 3. Copia o item guardado (NBT) do pacote aberto para o pacote selado
                        if (parcel.hasTag()) {
                            sealedParcel.setTag(parcel.getTag().copy());
                        }

                        // 4. Adiciona o Autor e o Destinatário
                        sealedParcel.getOrCreateTag().putString("Author", player.getName().getString());
                        sealedParcel.getOrCreateTag().putString("DestinatarioNome", recipient); // A tag agora bate com a da coruja!

                        // 5. Substitui os itens na mão do jogador
                        parcel.shrink(1);
                        if (!player.getInventory().add(sealedParcel)) {
                            player.drop(sealedParcel, false);
                        }

                        // Som de colocar lã/fio
                        player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.WOOL_PLACE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cFalta um Fio no inventário para amarrar o pacote!"), true);
                }
            }
        });
        return true;
    }
}