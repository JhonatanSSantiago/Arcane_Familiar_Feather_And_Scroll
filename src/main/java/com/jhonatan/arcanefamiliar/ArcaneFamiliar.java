package com.jhonatan.arcanefamiliar;

import com.jhonatan.arcanefamiliar.client.renderer.entity.OwlRenderer;
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

        // Registra os blocos e entidades de bloco
        com.jhonatan.arcanefamiliar.block.ModBlocks.BLOCKS.register(modEventBus);
        com.jhonatan.arcanefamiliar.block.ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);

        //menus
        com.jhonatan.arcanefamiliar.menu.ModMenuTypes.register(modEventBus);
        //entidades
        com.jhonatan.arcanefamiliar.entity.ModEntityTypes.register(modEventBus);

        //sons
        com.jhonatan.arcanefamiliar.sound.ModSounds.SOUND_EVENTS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) {
        // Aba de Ferramentas e Utilitários (Itens do Mod)
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.PARCHMENT);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.PARCEL);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.SEALED_PARCHMENT);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.MELTED_WAX);
            event.accept(com.jhonatan.arcanefamiliar.item.ModItems.WAX_SEAL);
            event.accept(ModItems.OWL_EGG);
        }

        // Aba de Blocos de Construção (ou Decoração) para o Ninho
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(com.jhonatan.arcanefamiliar.block.ModBlocks.OWL_NEST.get().asItem());
        }

        // Aba de Ovos de Invocação (Spawn Eggs)
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.OWL_SPAWN_EGG);
            // O do corvo será adicionado novamente quando criarmos a entidade do corvo
        }
    }

    private void commonSetup(final net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent event) {
        com.jhonatan.arcanefamiliar.network.ModMessages.register();

        event.enqueueWork(() -> {
            // Regista as regras de nascimento da nossa coruja (apenas no chão, ao ar livre)
            net.minecraft.world.entity.SpawnPlacements.register(
                    com.jhonatan.arcanefamiliar.entity.ModEntityTypes.OWL.get(),
                    net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    net.minecraft.world.entity.animal.Animal::checkAnimalSpawnRules
            );
        });
    }

    private void clientSetup(final net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent event) {
        // Liga o Menu Lógico ao Ecrã Visual
        net.minecraft.client.gui.screens.MenuScreens.register(
                com.jhonatan.arcanefamiliar.menu.ModMenuTypes.PARCEL_MENU.get(),
                com.jhonatan.arcanefamiliar.client.gui.ParcelScreen::new
        );
        net.minecraft.client.gui.screens.MenuScreens.register(
                com.jhonatan.arcanefamiliar.menu.ModMenuTypes.READ_PARCEL_MENU.get(),
                com.jhonatan.arcanefamiliar.client.gui.ReadParcelScreen::new
        );

        // Regista o visual da nossa coruja para o jogador ver
        net.minecraft.client.renderer.entity.EntityRenderers.register(
                com.jhonatan.arcanefamiliar.entity.ModEntityTypes.OWL.get(),
                OwlRenderer::new
        );
    }
}