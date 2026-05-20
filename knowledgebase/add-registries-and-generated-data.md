# Add Registries And Generated Data

Use this when adding blocks, items, tabs, recipes, tags, loot, models, or lang entries.

## Registry Pattern

Use NeoForge deferred registration patterns. Register holders on mod event bus during mod construction.

```java
public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
```

Use deferred holders from the register instead of eagerly constructing registry objects.

## Datagen Rule

Use datagen early when adding:

- blocks
- items
- recipes
- tags
- loot tables
- blockstates
- models
- language entries

Hand-authored JSON becomes brittle as content grows.

## What Not To Do

- Do not instantiate registry objects eagerly.
- Do not reference unregistered content statically.
- Do not hand-author large growing sets of models, tags, recipes, or loot tables when datagen can own them.
- Do not keep template example blocks/items/tabs once real mod behavior exists.
