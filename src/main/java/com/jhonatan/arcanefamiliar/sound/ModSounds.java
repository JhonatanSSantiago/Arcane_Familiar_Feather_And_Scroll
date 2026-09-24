package com.jhonatan.arcanefamiliar.sound;

import com.jhonatan.arcanefamiliar.ArcaneFamiliar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArcaneFamiliar.MODID);

    public static final RegistryObject<SoundEvent> COURIER_AMBIENT = registerSoundEvent("courier_ambient");
    public static final RegistryObject<SoundEvent> COURIER_HURT = registerSoundEvent("courier_hurt");
    public static final RegistryObject<SoundEvent> COURIER_DEATH = registerSoundEvent("courier_death");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(ArcaneFamiliar.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}