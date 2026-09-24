package com.jhonatan.arcanefamiliar.event;

import com.jhonatan.arcanefamiliar.entity.CourierEntity;
import com.jhonatan.arcanefamiliar.entity.ModEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// O "Bus.MOD" faz com que o Forge leia isto logo ao carregar o mod
@Mod.EventBusSubscriber(modid = "arcanefamiliar", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // Liga a vida e velocidade que criámos à nossa entidade Courier
        event.put(ModEntityTypes.COURIER.get(), CourierEntity.createAttributes().build());
    }
}