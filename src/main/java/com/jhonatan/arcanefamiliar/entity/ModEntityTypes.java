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

    // Registamos oficialmente a Coruja
    public static final RegistryObject<EntityType<OwlEntity>> OWL =
            ENTITY_TYPES.register("owl",
                    () -> EntityType.Builder.of(OwlEntity::new, MobCategory.CREATURE)
                            .sized(0.4f, 0.9f) // Tamanho da hitbox da coruja
                            .build("owl"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}