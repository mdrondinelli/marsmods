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

## Failed Approaches In This Project

These did not remove the recipe in observed testing:

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
