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

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MarsWorBloodFxConfig() {
    }
}
