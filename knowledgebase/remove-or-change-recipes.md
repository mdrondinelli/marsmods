# Remove Or Change Recipes

Use `ModifyRecipeJsonsEvent` for recipe removal in this project.

## Working API

```java
private static final Set<Identifier> REMOVED_RECIPES = Set.of(
        Identifier.withDefaultNamespace("wooden_axe"),
        Identifier.withDefaultNamespace("wooden_hoe"),
        Identifier.withDefaultNamespace("wooden_pickaxe"),
        Identifier.withDefaultNamespace("wooden_shovel"),
        Identifier.withDefaultNamespace("wooden_sword"),
        Identifier.withDefaultNamespace("stone_sword"));

@SubscribeEvent
public void removeWoodenToolRecipes(ModifyRecipeJsonsEvent event) {
    REMOVED_RECIPES.forEach(event.getRecipeJsons()::remove);
}
```

## Current Removed Recipe IDs

- `minecraft:wooden_axe`
- `minecraft:wooden_hoe`
- `minecraft:wooden_pickaxe`
- `minecraft:wooden_shovel`
- `minecraft:wooden_sword`
- `minecraft:stone_sword`

## Why File Overrides Fail (Root Cause)

`RecipeManager.prepare()` uses `SimpleJsonResourceReloadListener.scanDirectoryWithModifier()`, which calls `FileToIdConverter.listMatchingResources()` first. That call selects **one resource per file path** based on resource pack priority — highest-priority pack wins.

NeoForge (≥ 26.1.2.61-beta) bundles its own versions of all vanilla recipes in its JAR (modified to use tag-based ingredients like `c:rods/wooden`). The NeoForge JAR has higher resource pack priority than regular mod JARs. So when a mod provides `data/minecraft/recipe/torch.json` with `neoforge:never`, NeoForge's version of that file is selected instead — the mod's file is never loaded, and the condition is never evaluated.

`ModifyRecipeJsonsEvent` fires on the already-merged map (after priority resolution), so it can remove any entry regardless of source.

In NeoForge 26.1.2.36-beta, NeoForge had not yet moved vanilla recipes into its own JAR — the tag ingredient overhaul landed between .36 and .61 — so mod file overrides worked there.

## Failed Approaches In This Project

These did not remove the recipe in observed testing on NeoForge **26.1.2.61-beta**:

- empty file override at `src/main/resources/data/minecraft/recipe/<id>.json`
- JSON override with `neoforge:conditions` and `neoforge:never`

Use `ModifyRecipeJsonsEvent` unless there is a new verified reason not to.

For future recipe additions or balancing, prefer datapacks/datagen/KubeJS when practical. Use Java recipe mutation when runtime/project behavior proves resource-only approaches insufficient, as happened here.

## Testing Notes

- Full server restart worked.
- `/reload` did not apply observed recipe removal.
- OP status does not matter. If the recipe exists, OP and non-OP can craft it.
- Test actual crafting output slot, not only recipe book visuals.

## Recipe Book Unlocks

When adding or changing a player-facing recipe, consider the matching advancement under:

```text
data/<namespace>/advancement/recipes/<category>/<recipe_id>.json
```

The recipe JSON controls crafting or cooking behavior. The advancement controls when the recipe book unlocks that recipe. For vanilla recipe overrides, also override the vanilla advancement if the unlock criteria should match the new ingredients.

## What Not To Do

- Do not rely on recipe book disappearance as the only test.
- Do not assume old `recipes/` path; this target uses `recipe/` singular.
- Do not keep dead resource override files after switching to event-based removal.
