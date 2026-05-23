package me.mar.campfires;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MarsCampfiresConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MIN_BURN_TICKS = BUILDER
            .comment("Minimum random burn duration for newly lit campfires, in ticks.")
            .defineInRange("minBurnTicks", 4000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_BURN_TICKS = BUILDER
            .comment("Maximum random burn duration for newly lit campfires, in ticks.")
            .defineInRange("maxBurnTicks", 8000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue SCAN_INTERVAL_TICKS = BUILDER
            .comment("How often loaded campfire block entities are reconciled for missing expiry data and rain extinguishing.")
            .defineInRange("scanIntervalTicks", 20, 1, 1200);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MarsCampfiresConfig() {
    }
}
