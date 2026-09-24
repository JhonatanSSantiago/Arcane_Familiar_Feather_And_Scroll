package com.jhonatan.arcanefamiliar.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "arcanefamiliar");

    public static final RegistryObject<BlockEntityType<OwlNestBlockEntity>> OWL_NEST_BE =
            BLOCK_ENTITIES.register("owl_nest_be", () ->
                    BlockEntityType.Builder.of(OwlNestBlockEntity::new,
                            ModBlocks.OWL_NEST.get()).build(null));
}