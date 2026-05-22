# Marsfoodspoilage Test Plan

## Build And Load

1. Run from `marsfoodspoilage`:
   - `./gradlew compileJava`
   - `./gradlew build`
2. Launch a dev client/server and confirm:
   - Mod loads without registry/reload errors.
   - `data/marsfoodspoilage/spoilage_rules/baseline.json` is loaded.
   - No missing model/texture errors for overridden food item models.

## Freshness Basics

1. Give yourself several spoilable foods: raw meat, cooked meat, fish, bread, fruit/vegetable if tags exist.
2. Confirm food becomes unstackable.
3. Hover tooltips:
   - Fresh food shows `Fresh`.
   - After enough time, food shows `Stale`.
   - After remaining freshness reaches zero, food shows `Spoiled`.
4. Confirm tooltip time display changes from hours to days when appropriate.

## Spoilage Timing

1. Test a short-lived category like raw meat/fish.
2. Confirm transition timing follows:
   - `shelf_life_ticks`
   - `stale_threshold_ticks`
   - `timespeed` config
3. Change `timespeed`:
   - `2.0` should make spoilage happen twice as fast in game ticks.
   - `0.5` should make spoilage happen half as fast.

## Inventory And Storage Boundaries

1. Keep food in player inventory and wait; confirm it spoils.
2. Put food in a chest, wait, then open the chest; confirm elapsed time is applied.
3. Drop food on the ground; confirm it spoils while loaded.
4. Pick up stale/spoiled food; confirm state is updated before pickup.
5. Toss food from inventory; confirm state is preserved/updated.
6. If using container items, put food inside a container item and confirm nested contents get touched.

## Eating Behavior

1. Fresh food:
   - Eats at normal speed.
   - Applies normal vanilla hunger/saturation.
2. Stale food:
   - Eats slower according to `staleEatDurationMultiplier`, default `2.0`.
   - Still consumes normally.
3. Spoiled food:
   - Eats slower according to `spoiledEatDurationMultiplier`, default `4.0`.
   - Is not blocked.
   - Is consumed successfully.
   - Can apply configured negative effects after consumption.

## Spoiled Effects

1. Create a datapack rule under `data/<namespace>/spoilage_rules/test.json` with all chances set to `1.0`.
2. Eat spoiled food and confirm all apply:
   - Poison
   - Nausea
   - Weakness
   - Slowness
   - Hunger
3. Set all chances to `0.0`; confirm none apply.
4. Test mixed chances over repeated eating to confirm independent rolls.

## Datapack Rules

1. Confirm additive loading:
   - Mod baseline loads.
   - A datapack can add another `spoilage_rules/*.json`.
2. Confirm independent rule lists:
   - `spoilage` controls shelf life.
   - `spoiled_effects` controls consumption effects.
3. Confirm precedence:
   - Mod baseline loses to low-priority datapack.
   - Low-priority datapack loses to high-priority datapack.
   - Within the same pack/file, later matching entries win because matching resolves from the end.
4. Confirm malformed JSON fails loudly during reload.

## Does Not Spoil

1. Add an item to `marsfoodspoilage:does_not_spoil`.
2. Also make sure that item matches a broad spoilage tag like `c:foods`.
3. Confirm:
   - It receives no freshness component.
   - It does not become stale/spoiled.
   - It does not get stale/spoiled eat-duration changes.
   - It does not apply spoiled effects.

## Visuals

1. Check item models for supported foods:
   - Fresh model.
   - Stale model.
   - Spoiled model.
2. Confirm model state updates after inventory/container/drop/pickup refreshes.
3. Confirm unsupported foods still show tooltip/state correctly even without custom textures.

## Regression Checks

1. Non-food items are unaffected.
2. Food excluded from spoilage still behaves like vanilla food.
3. Creative mode consumption behavior remains sane.
4. Server/client sync works on multiplayer: one player sees the same freshness state another player sees after item transfer.
5. `/reload` updates datapack spoilage/effect rules without restarting.
