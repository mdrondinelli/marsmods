package me.mar.foodspoilage;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MarsSpoilageConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TIMESPEED = BUILDER
            .comment("Game time speed relative to vanilla. 1.0 = vanilla (24000 ticks/day). 2.0 = 2x faster: food spoils in half the ticks, display calibrated accordingly.")
            .defineInRange("timespeed", 1.0, 0.01, 1000.0);

    public static final ModConfigSpec.DoubleValue STALE_EAT_DURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for stale food eat duration. 1.0 = vanilla eat speed. 3.0 = stale food takes three times as long to eat.")
            .defineInRange("staleEatDurationMultiplier", 3.0, 1.0, 1000.0);

    public static final ModConfigSpec.DoubleValue SPOILED_EAT_DURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for spoiled food eat duration. 1.0 = vanilla eat speed. 4.0 = spoiled food takes four times as long to eat.")
            .defineInRange("spoiledEatDurationMultiplier", 4.0, 1.0, 1000.0);

    public static final ModConfigSpec.DoubleValue RACK_SPOILAGE_RATE = BUILDER
            .comment("Spoilage rate multiplier for food on the drying rack. 0.0 = no spoilage. 1.0 = normal rate.")
            .defineInRange("rackSpoilageRate", 0.0, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
