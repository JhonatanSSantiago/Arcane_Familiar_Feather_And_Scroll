package com.jhonatan.arcanefamiliar.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ParchmentItem extends Item {

    public ParchmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // Verificamos se estamos no "Cliente" (o computador do jogador que desenha o ecrã)
        if (level.isClientSide()) {
            // Futuramente: Código para abrir a Interface Gráfica (GUI) entrará aqui
            System.out.println("O jogador clicou com o pergaminho na mao!");
            net.minecraft.client.Minecraft.getInstance().setScreen(new com.jhonatan.arcanefamiliar.client.gui.ParchmentScreen());
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}