package com.jhonatan.arcanefamiliar.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ParcelMenu extends AbstractContainerMenu {
    private final Container parcelInventory;

    // Construtor chamado no Cliente (quando abre a interface)
    public ParcelMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(1));
    }

    // Construtor chamado no Servidor (lógica real)
    public ParcelMenu(int containerId, Inventory playerInventory, Container parcelInventory) {
        super(ModMenuTypes.PARCEL_MENU.get(), containerId);
        this.parcelInventory = parcelInventory;

        // Exige que o inventário do pacote tenha exatamente 1 slot
        checkContainerSize(parcelInventory, 1);
        parcelInventory.startOpen(playerInventory.player);

        // 1. O Slot do Pacote (Ficará no centro superior da interface)
        this.addSlot(new Slot(parcelInventory, 0, 80, 20));

        // 2. Os 27 Slots do Inventário Principal do Jogador
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, i * 18 + 51));
            }
        }

        // 3. Os 9 Slots da Hotbar (Barra de atalhos inferior) do Jogador
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 109));
        }
    }

    // Define se o jogador ainda tem o item na mão para manter o menu aberto
    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() == com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get() ||
                player.getOffhandItem().getItem() == com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get();
    }

    // Lógica essencial: O que acontece quando fazemos "Shift + Clique" num item
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            // Se o clique for no slot do pacote (índice 0), move para o inventário
            if (index == 0) {
                if (!this.moveItemStackTo(itemstack1, 1, 37, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Se o clique for no inventário, move para o pacote (apenas 1 item cabe!)
            else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemstack1);
        }
        return itemstack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.parcelInventory.stopOpen(player);
        // A gravação de itens é sempre feita do lado do servidor
        if (!player.level().isClientSide()) {
            ItemStack parcel = player.getMainHandItem();

            // Verifica se o pacote está na mão principal ou secundária
            if (parcel.getItem() != com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get()) {
                parcel = player.getOffhandItem();
            }

            if (parcel.getItem() == com.jhonatan.arcanefamiliar.item.ModItems.PARCEL.get()) {
                net.minecraft.nbt.CompoundTag nbt = parcel.getOrCreateTag();
                ItemStack storedItem = this.parcelInventory.getItem(0);

                if (!storedItem.isEmpty()) {
                    // Guarda o item colocado no NBT do pacote
                    nbt.put("StoredItem", storedItem.save(new net.minecraft.nbt.CompoundTag()));
                } else {
                    // Se o pacote estiver vazio, limpa a memória
                    nbt.remove("StoredItem");
                }
            }
        }
    }
}