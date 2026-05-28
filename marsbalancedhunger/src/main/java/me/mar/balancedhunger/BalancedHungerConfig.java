package me.mar.balancedhunger;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BalancedHungerConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue BLOCK_BREAK_EXHAUSTION = BUILDER
            .comment("Absolute exhaustion per block broken. Vanilla applies 0.005; if this is greater, the difference is added.")
            .defineInRange("blockBreakExhaustion", 0.05, 0.0, 10.0);

    public static final ModConfigSpec.DoubleValue WALK_PER_TICK = BUILDER
            .comment("Exhaustion per tick while walking on ground (movement input held, not sprinting or crouching).")
            .defineInRange("walkPerTick", 0.0005, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SPRINT_PER_TICK = BUILDER
            .comment("Exhaustion per tick while sprinting on ground.")
            .defineInRange("sprintPerTick", 0.0025, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue CROUCH_PER_TICK = BUILDER
            .comment("Exhaustion per tick while crouched and actively moving. Static crouch is free.")
            .defineInRange("crouchPerTick", 0.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue CLIMB_UP_PER_TICK = BUILDER
            .comment("Exhaustion per tick while climbing up (on ladder/vine/scaffolding with jump held).")
            .defineInRange("climbUpPerTick", 0.0025, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue CLIMB_DOWN_PER_TICK = BUILDER
            .comment("Exhaustion per tick while sliding down a climbable (no keys). Clinging with shift is free.")
            .defineInRange("climbDownPerTick", 0.0010, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue WADE_UP_PER_TICK = BUILDER
            .comment("Exhaustion per tick while in water (not swimming) and trying to rise (jump).")
            .defineInRange("wadeUpPerTick", 0.0025, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue WADE_HORIZONTAL_PER_TICK = BUILDER
            .comment("Exhaustion per tick while wading laterally in water (no vertical key).")
            .defineInRange("wadeHorizontalPerTick", 0.0015, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue WADE_DOWN_PER_TICK = BUILDER
            .comment("Exhaustion per tick while in water and intentionally sinking (shift).")
            .defineInRange("wadeDownPerTick", 0.0005, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SWIM_UP_PER_TICK = BUILDER
            .comment("Exhaustion per tick while swimming (prone) and rising (jump).")
            .defineInRange("swimUpPerTick", 0.0035, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SWIM_HORIZONTAL_PER_TICK = BUILDER
            .comment("Exhaustion per tick while swimming (prone) without vertical key input.")
            .defineInRange("swimHorizontalPerTick", 0.0025, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SWIM_DOWN_PER_TICK = BUILDER
            .comment("Exhaustion per tick while swimming (prone) and sinking (shift).")
            .defineInRange("swimDownPerTick", 0.0010, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue IDLE_PER_TICK = BUILDER
            .comment("Exhaustion per tick regardless of activity (metabolism). Applies during sleep timelapses too.")
            .defineInRange("idlePerTick", 0.0005, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private BalancedHungerConfig() {
    }
}
