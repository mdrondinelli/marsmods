package me.mar.bellows;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MarsBellows.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BELLOWS_USE =
            SOUND_EVENTS.register("bellows_use",
                    () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(MarsBellows.MODID, "bellows_use")));

    private ModSoundEvents() {
    }

    public static void register(IEventBus modBus) {
        SOUND_EVENTS.register(modBus);
    }
}
