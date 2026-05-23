# Mars Torches — Worldgen / Structure Replacement

## Current Status

Worldgen is **disabled**. Two classes exist (`TorchFeature`, `TorchBiomeModifier`) but are never wired into the game. Three data files were never written, so even uncommenting the registry entries would accomplish nothing:

- `data/marstorches/worldgen/configured_feature/replace_all_feature.json` — missing
- `data/marstorches/worldgen/placed_feature/replace_all_feature.json` — missing
- `data/marstorches/worldgen/biome_modifier/replace_torches.json` — missing

## Why It's Disabled

The existing `TorchFeature` approach is brute-force: it scans every block in a full chunk (16 × height × 16 ≈ 98k blocks) at `TOP_LAYER_MODIFICATION` time. Most chunks have zero torches. The overhead per chunk is disproportionate to the benefit.

The feature also has no clear value for the core gameplay loop: torches burn out when *players* interact with them. Unattended structure torches burning out on their own is a cosmetic bonus, not a mechanic players depend on.

## Why Players Don't Need This

**Players picking up a structure torch is already handled.** The mod overrides:

```
data/minecraft/loot_table/blocks/torch.json
```

with a `DropUnlitCondition` predicate. When `vanillaTorchesDropUnlit=true` (default), breaking any vanilla `minecraft:torch` drops an `marstorches:unlit_torch` item instead. When the player places it, it becomes a mod block that burns out normally.

So the only gap is: a torch sitting in a dungeon, never touched by a player, burning out on its own over time. Low priority.

## Better Future Approach: StructurePlaceEvent

Instead of scanning every chunk at worldgen time, hook the NeoForge `StructurePlaceEvent` (or equivalent in the target Minecraft version). This fires once per structure piece as it is placed, passing the structure's blocks. Intercept torch placements at that point and substitute mod blocks.

Benefits over `TorchFeature`:
- Fires only when a structure actually generates — zero cost for non-structure chunks
- Works on newly generated worlds without requiring a biome modifier / placed feature data stack
- Avoids the chunk-scan ordering issues

Rough implementation sketch:

```java
@SubscribeEvent
public void onStructurePlaced(StructurePlaceEvent event) {
    // iterate event.getBoundingBox() or the structure piece blocks
    // replace Blocks.TORCH / Blocks.WALL_TORCH with mod equivalents
    // preserve HORIZONTAL_FACING for wall torches
    // schedule TICK_INTERVAL tick on each replaced block
}
```

Verify the exact event name / API surface for the target NeoForge version before implementing — event names have shifted across major versions.
