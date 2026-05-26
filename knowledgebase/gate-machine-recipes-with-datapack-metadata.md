# Gate Machine Recipes With Datapack Metadata

Use this when a vanilla recipe type needs extra mod-specific metadata, such as melting points.

## Reload Listener

Register a `SimplePreparableReloadListener` from `AddServerReloadListenersEvent` on `NeoForge.EVENT_BUS`.

Use a dedicated data directory such as:

```text
data/<modid>/melting_points/*.json
```

Support exact recipe rules and ingredient rules. Exact recipe ids should win over ingredient matches.

## Rule Shape

Single rule:

```json
{
  "ingredient": "minecraft:clay_ball",
  "melting_point": 600
}
```

Bundled rules:

```json
{
  "values": [
    { "ingredient": "minecraft:clay_ball", "melting_point": 600 },
    { "recipe": "minecraft:iron_ingot_from_smelting_raw_iron", "melting_point": 1538 }
  ]
}
```

For simple item/tag rules inside a plain reload listener, parse strings directly:

- `"minecraft:clay_ball"` for one item.
- `"#minecraft:logs"` for a tag.

Then test with `ItemStack.is(ResourceKey<Item>)` or `ItemStack.is(TagKey<Item>)` through the holder predicate APIs.

## Machine Tick

If a machine must consume fuel even when the recipe is blocked, do not delegate to vanilla `AbstractFurnaceBlockEntity.serverTick`. Own the tick loop and gate only cook-progress advancement:

```java
if (isLit && requiredTemperature <= currentTemperature) {
    cookingTimer++;
}
```

Keep fuel burn, lit state, output checks, and cook progress as separate decisions.
