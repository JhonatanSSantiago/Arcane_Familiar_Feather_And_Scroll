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

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.PARCHMENT);
        }
    }


}