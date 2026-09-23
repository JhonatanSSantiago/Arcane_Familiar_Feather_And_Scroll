package com.jhonatan.arcanefamiliar.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ReadParcelMenu extends AbstractContainerMenu {
    private final Container parcelInventory;
    private final String author;
    private final String recipient;

    // Construtor do Cliente
    public ReadParcelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1), extraData.readUtf(), extraData.readUtf());
    }

    // Construtor do Servidor
    public ReadParcelMenu(int containerId, Inventory playerInventory, Container parcelInventory, String author, String recipient) {
        super(ModMenuTypes.READ_PARCEL_MENU.get(), containerId);
        this.parcelInventory = parcelInventory;
        this.author = author;
        this.recipient = recipient;

        checkContainerSize(parcelInventory, 1);
        parcelInventory.startOpen(playerInventory.player);

        // 1. Slot central do Pacote Selado
        this.addSlot(new Slot(parcelInventory, 0, 80, 20));

        // 2. Slots do Inventário do Jogador
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 109));
        }
    }

    public String getAuthor() { return author; }
    public String getRecipient() { return recipient; }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get() ||
                player.getOffhandItem().getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) return ItemStack.EMPTY;
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
            if (itemstack1.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
            if (itemstack1.getCount() == itemstack.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.parcelInventory.stopOpen(player);

        if (!player.level().isClientSide()) {
            InteractionHand hand = player.getMainHandItem().getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack sealedParcel = player.getItemInHand(hand);

            if (sealedParcel.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCEL.get()) {
                ItemStack storedItem = this.parcelInventory.getItem(0);

                if (storedItem.isEmpty()) {
                    // MECÂNICA: Se o pacote ficou vazio, quebra o selo e devolve um pacote normal
                    player.setItemInHand(hand, new ItemStack(com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get()));

                    // Som do fio a rebentar indicando que o pacote foi aberto
                    player.level().playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.WOOL_BREAK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.2F);
                } else {
                    // Mantém selado e guarda o item no NBT caso o jogador tenha fechado sem retirar tudo
                    sealedParcel.getOrCreateTag().put("StoredItem", storedItem.save(new net.minecraft.nbt.CompoundTag()));
                }
            }
        }
    }
}