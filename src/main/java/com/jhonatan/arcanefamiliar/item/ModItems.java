package com.jhonatan.arcanefamiliar.item;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // Cria a lista de registro informando ao Forge que vamos adicionar Itens no nosso MODID
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ArcaneFamiliar.MODID);

    // Agora chamamos PARCHMENT
    public static final RegistryObject<Item> PARCHMENT = ITEMS.register("parchment",
            () -> new Item(new Item.Properties()));

    //pacote
    public static final RegistryObject<Item> PARCEL = ITEMS.register("parcel",
            () -> new Item(new Item.Properties()));

    // Método para conectar a nossa lista de itens ao carregamento inicial do Minecraft
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}