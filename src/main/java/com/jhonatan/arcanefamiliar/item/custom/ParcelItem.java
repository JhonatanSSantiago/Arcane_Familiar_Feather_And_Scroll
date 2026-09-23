package com.jhonatan.arcanefamiliar.item.custom;

import com.jhonatan.arcanefamiliar.menu.ParcelMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class ParcelItem extends Item {
    public ParcelItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // As interfaces de inventário são abertas APENAS pelo servidor
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ItemStack parcel = player.getItemInHand(hand);
            // Criamos um contentor temporário de 1 slot
            SimpleContainer inventory = new SimpleContainer(1);

            // LER A MEMÓRIA: Se o pacote já tiver um item guardado, carrega-o para o contentor
            if (parcel.hasTag() && parcel.getTag().contains("StoredItem")) {
                inventory.setItem(0, ItemStack.of(parcel.getTag().getCompound("StoredItem")));
            }

            // Pede ao Forge para abrir a interface
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (id, playerInv, p) -> new ParcelMenu(id, playerInv, inventory),
                    Component.literal("Pacote Arcano")
            ));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}