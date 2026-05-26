# Add A Craftable Item

Use this when adding a plain registered item with model, texture, lang, and recipe data.

## Item Data Files

For Minecraft `26.1.2`, item assets in this repo use both:

- `assets/<modid>/items/<item>.json` to point at the item model.
- `assets/<modid>/models/item/<item>.json` to point at `textures/item/<item>.png`.

## Mirrored Shaped Recipes

`minecraft:crafting_shaped` does not make one JSON recipe match both horizontal orientations. If a recipe should be mirrored, add a second recipe JSON with the mirrored `pattern` and the same `result`.

For recipes using any wooden slab, use the vanilla tag:

```json
"W": "#minecraft:wooden_slabs"
```
