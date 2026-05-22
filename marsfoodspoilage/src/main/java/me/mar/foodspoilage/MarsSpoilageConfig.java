package me.mar.foodspoilage;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MarsSpoilageConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TIMESPEED = BUILDER
            .comment("Game time speed relative to vanilla. 1.0 = vanilla (24000 ticks/day). 2.0 = 2x faster: food spoils in half the ticks, display calibrated accordingly.")
            .defineInRange("timespeed", 1.0, 0.01, 1000.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
