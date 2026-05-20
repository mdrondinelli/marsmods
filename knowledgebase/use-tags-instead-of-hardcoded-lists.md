# Use Tags Instead Of Hardcoded Lists

Use tags when rule should work across modded content.

## Working Pattern

```java
if (event.getState().is(BlockTags.LOGS)) {
    // Applies to vanilla logs and modded logs that correctly join minecraft:logs.
}
```

For this project, `BlockTags.LOGS` is the compatibility API for "any logs, even modded."

Tags are semantic APIs between mods, datapacks, and KubeJS. Prefer tags for concepts such as primitive fuels, ritual metals, machine parts, or seasonal foods when other packs might add content.

## When Exact IDs Are OK

Exact vanilla IDs are acceptable when the rule is deliberately vanilla-only.

Example:

```java
event.getTargetBlock().is(Blocks.STONE)
```

This is appropriate for "breaking vanilla stone with flint." It does not claim to cover every stone-like block.

## What Not To Do

- Do not list every known modded log in Java.
- Do not create a hard dependency on another mod just to recognize common semantic groups.
- Do not broaden exact requirements into tags unless design wants broad compatibility.
- Do not use tags as a substitute for clear progression intent; tags define semantic groups, not balance by themselves.
- Do not require source edits when KubeJS/datapacks could orchestrate progression through tags.

## Future Notes

When adding new compatibility rules, first decide whether behavior is:

- semantic and modpack-friendly: use a tag
- exact vanilla behavior: use exact block/item ID
- pack-controlled progression: expose/tag data and let datapacks or KubeJS compose it
