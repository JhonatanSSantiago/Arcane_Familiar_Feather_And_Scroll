package com.jhonatan.arcanefamiliar.menu;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ArcaneFamiliar.MODID);

    public static final RegistryObject<MenuType<ParcelMenu>> PARCEL_MENU =
            MENUS.register("parcel_menu", () -> IForgeMenuType.create(ParcelMenu::new));

    public static final RegistryObject<MenuType<ReadParcelMenu>> READ_PARCEL_MENU =
            MENUS.register("read_parcel_menu", () -> IForgeMenuType.create(ReadParcelMenu::new));

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}