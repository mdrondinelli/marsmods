package me.mar.originguard;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;

public class OriginGuardConfig {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue RADIUS_BLOCKS = BUILDER
            .comment("Block radius around (0,0) within which configured structures will not generate. Measured by chunk-centre distance.")
            .defineInRange("radiusBlocks", 2000, 0, 1_000_000);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_STRUCTURES = BUILDER
            .comment(
                    "Structures suppressed inside the radius.",
                    "Entries can be a registry ID (e.g. \"minecraft:mansion\") or a tag prefixed with '#' (e.g. \"#minecraft:village\").",
                    "Tag matching covers any structure (vanilla or modded) the tag includes."
            )
            .defineList("blockedStructures", List.of(
                    "#minecraft:village",
                    "minecraft:mansion",
                    "minecraft:monument",
                    "minecraft:pillager_outpost",
                    "minecraft:ancient_city",
                    "minecraft:trial_chambers"
            ), () -> "minecraft:mansion", o -> o instanceof String);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
