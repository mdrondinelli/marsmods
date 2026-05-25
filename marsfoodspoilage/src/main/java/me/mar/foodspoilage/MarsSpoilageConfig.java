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

    public static final ModConfigSpec.DoubleValue STALE_NUTRITION_MULTIPLIER = BUILDER
            .comment("Multiplier for stale food nutrition (hunger restored). 1.0 = vanilla. 0.75 = 75% of fresh nutrition.")
            .defineInRange("staleNutritionMultiplier", 0.75, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SPOILED_NUTRITION_MULTIPLIER = BUILDER
            .comment("Multiplier for spoiled food nutrition (hunger restored). 1.0 = vanilla. 0.25 = 25% of fresh nutrition.")
            .defineInRange("spoiledNutritionMultiplier", 0.25, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue STALE_SATURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for stale food saturation. 1.0 = vanilla. 0.5 = half saturation.")
            .defineInRange("staleSaturationMultiplier", 0.5, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SPOILED_SATURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for spoiled food saturation. 1.0 = vanilla. 0.0 = no saturation.")
            .defineInRange("spoiledSaturationMultiplier", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue RACK_SPOILAGE_RATE = BUILDER
            .comment("Spoilage rate multiplier for food on the drying rack. 0.0 = no spoilage. 1.0 = normal rate.")
            .defineInRange("rackSpoilageRate", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue CAMPFIRE_SPOILAGE_RATE = BUILDER
            .comment("Spoilage rate multiplier for food on a lit campfire. 0.0 = paused. 1.0 = normal rate.")
            .defineInRange("campfireSpoilageRate", 0.0, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
