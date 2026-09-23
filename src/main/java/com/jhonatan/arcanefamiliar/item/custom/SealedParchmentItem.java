package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SealedParchmentItem extends Item {

    public SealedParchmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        // Lê a base de dados (NBT) do item
        CompoundTag nbt = stack.getTag();

        if (nbt != null) {
            // Se tiver autor, adiciona uma linha amarela
            if (nbt.contains("Author")) {
                tooltip.add(Component.literal("§7Assinado por: §e" + nbt.getString("Author")));
            }
            // Se tiver mensagem, adiciona uma linha cinzenta
            if (nbt.contains("MessageText")) {
                tooltip.add(Component.literal("§8" + nbt.getString("MessageText")));
            }
        }
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}