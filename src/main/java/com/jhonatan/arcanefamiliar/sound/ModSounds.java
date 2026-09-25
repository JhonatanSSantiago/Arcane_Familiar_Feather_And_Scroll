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

    // Sons da Coruja
    public static final RegistryObject<SoundEvent> OWL_AMBIENT = registerSoundEvent("owl_ambient");
    public static final RegistryObject<SoundEvent> OWL_HURT = registerSoundEvent("owl_hurt");
    public static final RegistryObject<SoundEvent> OWL_DEATH = registerSoundEvent("owl_death");

    // Sons do Corvo
    public static final RegistryObject<SoundEvent> CROW_AMBIENT = registerSoundEvent("crow_ambient");
    public static final RegistryObject<SoundEvent> CROW_HURT = registerSoundEvent("crow_hurt");
    public static final RegistryObject<SoundEvent> CROW_DEATH = registerSoundEvent("crow_death");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = new ResourceLocation(ArcaneFamiliar.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}