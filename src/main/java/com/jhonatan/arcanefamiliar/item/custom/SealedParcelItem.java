package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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
        // Lê o NBT para mostrar o destinatário na Tooltip
        if (stack.hasTag() && stack.getTag().contains("Recipient")) {
            String recipient = stack.getTag().getString("Recipient");
            tooltip.add(Component.literal("§7Para: §f" + recipient));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (!level.isClientSide() && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ItemStack parcel = player.getItemInHand(hand);

            // Som de manusear um embrulho/tecido ao abrir a interface
            level.playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);

            String author = parcel.hasTag() && parcel.getTag().contains("Author") ? parcel.getTag().getString("Author") : "Desconhecido";
            String recipient = parcel.hasTag() && parcel.getTag().contains("Recipient") ? parcel.getTag().getString("Recipient") : "Desconhecido";

            net.minecraft.world.SimpleContainer inventory = new net.minecraft.world.SimpleContainer(1);
            if (parcel.hasTag() && parcel.getTag().contains("StoredItem")) {
                inventory.setItem(0, ItemStack.of(parcel.getTag().getCompound("StoredItem")));
            }

            net.minecraftforge.network.NetworkHooks.openScreen(serverPlayer, new net.minecraft.world.SimpleMenuProvider(
                    (id, playerInv, p) -> new com.jhonatan.arcanefamiliar.menu.ReadParcelMenu(id, playerInv, inventory, author, recipient),
                    Component.literal("Pacote Selado")
            ), buf -> {
                buf.writeUtf(author);
                buf.writeUtf(recipient);
            });
        }
        return net.minecraft.world.InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}