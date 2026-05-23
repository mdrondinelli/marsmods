package me.mar.torches.config;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TorchConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue TORCH_BURNOUT_TIME = BUILDER
            .comment("Torch burn duration in minutes. -1 disables burnout.")
            .defineInRange("torchBurnoutTime", 60, -1, 240);

    public static final int MATCHBOX_DURABILITY_MAX = 512;

    public static final ModConfigSpec.IntValue MATCHBOX_DURABILITY = BUILDER
            .comment("Matchbox uses before breaking. -1 = unlimited.")
            .defineInRange("matchboxDurability", 64, -1, MATCHBOX_DURABILITY_MAX);

    public static final ModConfigSpec.BooleanValue NO_RELIGHT_ENABLED = BUILDER
            .comment("True: burned-out torches disappear. False: become unlit.")
            .define("torchNoRelight", false);

    public static final ModConfigSpec.BooleanValue MATCHBOX_CREATES_FIRE = BUILDER
            .define("matchboxCreatesFire", false);

    public static final ModConfigSpec.BooleanValue VANILLA_TORCHES_DROP_UNLIT = BUILDER
            .define("vanillaTorchesDropUnlit", true);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> LIGHT_TORCH_ITEMS = BUILDER
            .comment("Extra items that can light placed torches. E.g. [\"minecraft:lava_bucket\"]")
            .defineListAllowEmpty("lightTorchItems", List.of(), e -> e instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private TorchConfig() {}
}
