package com.jhonatan.arcanefamiliar.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "arcanefamiliar");

    public static final RegistryObject<EntityType<CourierEntity>> COURIER =
            ENTITY_TYPES.register("courier",
                    () -> EntityType.Builder.of(CourierEntity::new, MobCategory.CREATURE)
                            .sized(0.4f, 0.9f) // Tamanho da "caixa" invisível do bicho (similar a um papagaio)
                            .build("courier"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}