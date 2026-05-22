package me.mar.foodspoilage;

import net.neoforged.neoforge.common.ModConfigSpec;

public class MarsSpoilageConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SHELF_LIFE_MULTIPLIER = BUILDER
            .comment("Multiplier applied to all shelf life and stale threshold values. 2.0 = food lasts twice as long.")
            .defineInRange("shelfLifeMultiplier", 1.0, 0.01, 100.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
