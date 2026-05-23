package me.mar.worbloodfx;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MarsWorBloodFxConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_PARTICLES = BUILDER
            .comment("Whether blood particles are spawned when bleeding entities take damage.")
            .define("enableParticles", true);

    public static final ModConfigSpec.BooleanValue ENABLE_GROUND_SPLATTERS = BUILDER
            .comment("Whether short-lived ground blood splatters are spawned when bleeding entities take damage on valid ground.")
            .define("enableGroundSplatters", true);

    public static final ModConfigSpec.IntValue HIT_PARTICLE_COUNT = BUILDER
            .comment("Number of red block particles spawned at the damaged entity.")
            .defineInRange("hitParticleCount", 10, 0, 1000);

    public static final ModConfigSpec.DoubleValue GROUND_SPLATTER_CHANCE = BUILDER
            .comment("Chance that a valid damaging hit creates a ground blood splatter.")
            .defineInRange("groundSplatterChance", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.IntValue GROUND_SPLATTER_LIFETIME_TICKS = BUILDER
            .comment("Lifetime of ground blood splatters in ticks.")
            .defineInRange("groundSplatterLifetimeTicks", 6000, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue BRUSH_CLEANUP_CHANCE = BUILDER
            .comment("Chance that brushing a blood splatter removes it.")
            .defineInRange("brushCleanupChance", 0.035, 0.0, 1.0);

    public static final ModConfigSpec.IntValue MAX_GROUND_SPLATTERS_PER_CHUNK = BUILDER
            .comment("Maximum blood splatter display entities allowed in a chunk.")
            .defineInRange("maxGroundSplattersPerChunk", 64, 0, 4096);

    public static final ModConfigSpec.DoubleValue ARMOR_FX_REDUCTION_PER_POINT = BUILDER
            .comment("Blood FX chance reduction per armor point on the damaged entity.")
            .defineInRange("armorFxReductionPerPoint", 0.03, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue MAX_ARMOR_FX_REDUCTION = BUILDER
            .comment("Maximum total blood FX chance reduction from armor.")
            .defineInRange("maxArmorFxReduction", 0.85, 0.0, 1.0);

    public static final ModConfigSpec.BooleanValue ENABLE_BLEEDING_TRAILS = BUILDER
            .comment("Whether meaningful hits start short cosmetic bleeding trails.")
            .define("enableBleedingTrails", true);

    public static final ModConfigSpec.DoubleValue MIN_BLEED_DAMAGE = BUILDER
            .comment("Minimum final damage needed to start cosmetic bleeding. Minecraft damage units are half-hearts.")
            .defineInRange("minBleedDamage", 2.0, 0.0, 1000.0);

    public static final ModConfigSpec.IntValue BLEED_BASE_DURATION_TICKS = BUILDER
            .comment("Base cosmetic bleed duration after a qualifying hit.")
            .defineInRange("bleedBaseDurationTicks", 80, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue BLEED_DURATION_PER_DAMAGE = BUILDER
            .comment("Additional cosmetic bleed duration per point of final damage.")
            .defineInRange("bleedDurationPerDamage", 30.0, 0.0, 10000.0);

    public static final ModConfigSpec.IntValue BLEED_MAX_DURATION_TICKS = BUILDER
            .comment("Maximum cosmetic bleed duration after repeated or heavy hits.")
            .defineInRange("bleedMaxDurationTicks", 400, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue BLEED_INTERVAL_TICKS = BUILDER
            .comment("How often active cosmetic bleeding tries to drip.")
            .defineInRange("bleedIntervalTicks", 20, 1, 1200);

    public static final ModConfigSpec.DoubleValue BLEED_DRIP_CHANCE = BUILDER
            .comment("Chance each bleed interval creates a drip particle and possible trail splatter.")
            .defineInRange("bleedDripChance", 0.75, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue TRAIL_MIN_DISTANCE = BUILDER
            .comment("Minimum movement since the last trail splatter before another moving trail splatter can spawn.")
            .defineInRange("trailMinDistance", 0.75, 0.0, 64.0);

    public static final ModConfigSpec.IntValue STATIONARY_SPLATTER_INTERVAL_TICKS = BUILDER
            .comment("Minimum time between trail splatters for a bleeding entity that is standing mostly still.")
            .defineInRange("stationarySplatterIntervalTicks", 100, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MAX_BLEEDING_ENTITIES_PER_LEVEL = BUILDER
            .comment("Maximum entities tracked for cosmetic bleeding per level.")
            .defineInRange("maxBleedingEntitiesPerLevel", 128, 0, 4096);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MarsWorBloodFxConfig() {
    }
}
