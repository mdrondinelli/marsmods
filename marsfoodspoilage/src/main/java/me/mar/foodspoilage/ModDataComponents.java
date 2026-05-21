package me.mar.foodspoilage;

import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    private static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MarsFoodSpoilage.MODID);

    public static final Supplier<DataComponentType<FreshnessData>> FRESHNESS = REGISTRAR.registerComponentType(
            "freshness",
            builder -> builder.persistent(FreshnessData.CODEC).networkSynchronized(FreshnessData.STREAM_CODEC));

    private ModDataComponents() {
    }

    public static void register(IEventBus modBus) {
        REGISTRAR.register(modBus);
    }
}
