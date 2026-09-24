package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SealedParcelItem extends Item {

    public SealedParcelItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag() != null) {
            if (stack.getTag().contains("Author")) {
                tooltip.add(Component.literal("§7De: §f" + stack.getTag().getString("Author")));
            }
            if (stack.getTag().contains("DestinatarioNome")) {
                tooltip.add(Component.literal("§7Para: §f" + stack.getTag().getString("DestinatarioNome")));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack parcel = player.getItemInHand(hand);

        // Toda a lógica acontece no Servidor
        if (!level.isClientSide()) {

            // 1. OUVE O BARULHO DE RASGANDO
            // Misturamos som de papel e som de quebra para parecer cartão a rasgar
            level.playSound(null, player.blockPosition(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);
            level.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, net.minecraft.sounds.SoundSource.PLAYERS, 0.6F, 0.8F);

            // 2. OS ITENS APARECEM NO INVENTÁRIO
            if (parcel.hasTag() && parcel.getTag().contains("StoredItem")) {
                ItemStack itemGuardado = ItemStack.of(parcel.getTag().getCompound("StoredItem"));

                // Tenta colocar no inventário, se estiver cheio, cai no chão
                if (!player.getInventory().add(itemGuardado)) {
                    player.drop(itemGuardado, false);
                }
            }

            // 3. O PACOTE SELADO É TROCADO PELO ABERTO
            ItemStack pacoteAberto = new ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.OPENED_PARCEL.get());

            // Copia quem enviou/recebeu para a caixa vazia, mas remove o item de dentro
            if (parcel.hasTag()) {
                pacoteAberto.setTag(parcel.getTag().copy());
                pacoteAberto.getTag().remove("StoredItem");
            }

            // Consome 1 pacote selado da mão do jogador
            if (!player.getAbilities().instabuild) {
                parcel.shrink(1);
            }

            // Coloca o pacote aberto na mão (ou no inventário se a mão já tiver outra coisa)
            if (parcel.isEmpty()) {
                player.setItemInHand(hand, pacoteAberto);
            } else {
                if (!player.getInventory().add(pacoteAberto)) {
                    player.drop(pacoteAberto, false);
                }
            }
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}