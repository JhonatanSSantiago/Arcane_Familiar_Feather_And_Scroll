package com.jhonatan.arcanefamiliar.item;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import com.jhonatan.arcanefamiliar.item.custom.OpenedParcelItem;
import com.jhonatan.arcanefamiliar.item.custom.OpenedParchmentItem;
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
            () -> new com.jhonatan.arcanefamiliar.item.custom.ParchmentItem(new Item.Properties().stacksTo(1)));

    //pacote
    public static final RegistryObject<Item> PARCEL = ITEMS.register("parcel",
            () -> new com.jhonatan.arcanefamiliar.item.custom.ParcelItem(new Item.Properties().stacksTo(1)));

    // O pergaminho final já fechado com a mensagem
    public static final RegistryObject<Item> SEALED_PARCHMENT = ITEMS.register("sealed_parchment",
            () -> new com.jhonatan.arcanefamiliar.item.custom.SealedParchmentItem(new Item.Properties().stacksTo(1)));

    //pacote fechado e selado
    public static final RegistryObject<Item> SEALED_PARCEL = ITEMS.register("sealed_parcel",
            () -> new com.jhonatan.arcanefamiliar.item.custom.SealedParcelItem(new Item.Properties().stacksTo(1)));

    // A cera derretida que sai da fornalha
    public static final RegistryObject<Item> MELTED_WAX = ITEMS.register("melted_wax",
            () -> new Item(new Item.Properties()));

    // O selo final (cera derretida + corante + linha)
    public static final RegistryObject<Item> WAX_SEAL = ITEMS.register("wax_seal",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> OPENED_PARCHMENT = ITEMS.register("opened_parchment",
            () -> new OpenedParchmentItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> OPENED_PARCEL = ITEMS.register("opened_parcel",
            () -> new OpenedParcelItem(new Item.Properties().stacksTo(1)));

    // Método para conectar a nossa lista de itens ao carregamento inicial do Minecraft
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}