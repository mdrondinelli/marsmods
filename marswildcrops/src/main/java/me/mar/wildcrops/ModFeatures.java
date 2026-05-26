package me.mar.wildcrops;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, MarsWildCrops.MODID);

    public static final DeferredHolder<Feature<?>, Feature<SimpleBlockConfiguration>> WILD_CROP_PATCH =
            FEATURES.register("wild_crop_patch", () -> new WildCropPatchFeature(SimpleBlockConfiguration.CODEC));

    private ModFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
