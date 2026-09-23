package com.jhonatan.arcanefamiliar;

import com.jhonatan.arcanefamiliar.item.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArcaneFamiliar.MODID)
public class ArcaneFamiliar {

    public static final String MODID = "arcanefamiliar";

    public ArcaneFamiliar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Registra os itens do mod
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        com.jhonatan.arcanefamiliar.menu.ModMenuTypes.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.PARCHMENT);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.PARCEL);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.MELTED_WAX);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.WAX_SEAL);
        }
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        com.jhonatan.arcanefamiliar.network.ModMessages.register();
    }

    private void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        // Liga o Menu Lógico ao Ecrã Visual
        net.minecraft.client.gui.screens.MenuScreens.register(
                com.jhonatan.arcanefamiliar.menu.ModMenuTypes.PARCEL_MENU.get(),
                com.jhonatan.arcanefamiliar.client.gui.ParcelScreen::new
        );
        // Adicione esta nova linha:
        net.minecraft.client.gui.screens.MenuScreens.register(
                com.jhonatan.arcanefamiliar.menu.ModMenuTypes.READ_PARCEL_MENU.get(),
                com.jhonatan.arcanefamiliar.client.gui.ReadParcelScreen::new
        );
    }


}