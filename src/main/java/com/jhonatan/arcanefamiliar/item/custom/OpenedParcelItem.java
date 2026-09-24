package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OpenedParcelItem extends Item {

    public OpenedParcelItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (stack.hasTag() && stack.getTag() != null) {
            // Mostra o Remetente
            if (stack.getTag().contains("Author")) {
                tooltip.add(Component.literal("§7De: §f" + stack.getTag().getString("Author")));
            }
            // Mostra o Destinatário
            if (stack.getTag().contains("DestinatarioNome")) {
                tooltip.add(Component.literal("§7Para: §f" + stack.getTag().getString("DestinatarioNome")));
            }
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }
}