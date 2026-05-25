# Playtest Notes

Date: 2026-05-24
Pack/build:
World seed:
Mode:
Tester:

## Session Goals

- Start a fresh survival world and check whether the first 30 minutes of progression are understandable without commands.
- Verify that recipe removals, replacement recipes, and item drops make the early game possible.
- Watch for systems fighting each other: torches, campfires, food spoilage, sleep, and compatibility overrides.
- Record every confusing moment as an issue, even if it is technically working.

## Smoke Checklist

- Game reaches title screen with the full local pack.
- New world creates without datapack or registry errors.
- Existing world loads after restart.
- Client log has no repeated errors during normal play.
- All custom items and blocks have names, icons, models, and expected creative tab placement.
- Recipe book and JEI-like recipe views, if present, do not expose removed or dead-end recipes.

## Early Progression

- Can collect or craft the intended first tool.
- Can obtain wood, stone, and basic crafting materials without bypassing the intended flow.
- Removed vanilla recipes do not leave advancement toasts or recipe unlocks pointing at disabled paths.
- Tool speed, durability, repair, and harvest level feel strict but not stalled.
- Death and respawn do not strand the player without access to core materials.

Notes:

- 

## Flint Tool

- Flint tool recipe appears only when intended.
- Flint tool can harvest every block it is supposed to unlock.
- Flint tool cannot harvest blocks tagged as incorrect for the tool.
- Repair material works and does not accept unrelated items.
- Block breaking speed communicates the intended role compared with vanilla tools.

Notes:

- 

## Stone Age Recipes

- Furnace recipe removal behaves as expected.
- Brick from campfire cooking works.
- No recipe conflicts with other installed mods.
- Advancements and recipe unlocks match the new progression.

Notes:

- 

## Torches

- Vanilla torch crafting is replaced by the intended lit/unlit torch path.
- Lit, smoldering, and unlit torch states render correctly in item and world forms.
- Wall torches keep the correct state when placed, updated, broken, and restarted.
- Matchbox use feels discoverable and consumes durability or items as intended.
- Torch drops do not duplicate or delete items unexpectedly.

Notes:

- 

## Campfires

- Campfire burn timing matches the intended pacing.
- Campfire state persists through save/load.
- Cooking, relighting, and extinguishing behavior is consistent.
- Config defaults produce sane survival gameplay.
- Campfires interact cleanly with Stone Age brick cooking.

Notes:

- 

## Food Spoilage

- Freshness tooltip is readable and updates at a useful cadence.
- Fresh, stale, spoiled, dried, and non-spoiling foods follow their rules.
- Drying rack placement, model, block entity rendering, insertion, removal, and completion work.
- Spoiled effects trigger correctly and do not apply to exempt foods.
- Spoilage state persists across save/load, stack splits, crafting, death, and container movement.

Notes:

- 

## Sleep Overhaul

- Bed interaction works at night and follows the configured daytime behavior.
- Sleep UI and time-lapse behavior are understandable.
- Hunger, damage, wake-up, and multiplayer-like edge cases do not soft-lock the player.
- Client and server state stay synchronized after reconnect or world reload.

Notes:

- 

## Blood FX

- Damage spawns effects only when configured and appropriate.
- Effects are cosmetic only and do not affect gameplay state.
- Particle amount is noticeable without becoming noisy in combat.
- Config changes apply as expected.

Notes:

- 

## Compatibility

- External mods load without missing dependency or duplicate registration errors.
- Recipes, tags, and loot tables from compatibility mods do not undo progression changes.
- Datapack reload works from a loaded world.
- Modded ores, foods, tools, and blocks follow tag-based behavior where possible.

Notes:

- 

## Issues

Use this format for anything that needs code or data follow-up:

### P0/P1/P2 - Short title

- Area:
- Steps:
- Expected:
- Actual:
- Evidence:
- Suspected file:
- Fix idea:

## Raw Timeline

- Drying rack should not work when raining
- No wild potatoes or carrots
- Sleeping is a little buggy
- Everything should drop bones
- String?
- Check if food spoilage is working in furnaces
- Spider eyes should not spoil
- Spider eyes and rotten flesh should stack.
- Food shouldn't spoil on campfires
- Spoiled food should give less hunger and saturation
- Spoiled food should give less nutritional value for the nutritional balanace mod.
- Block breaking should cost hunger
- Weird interaction between bow in hand and sleeping on leaf litter
- Leaf litter doesn't set spawn
