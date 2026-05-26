# Add Wild Crop Generation

`wilos-overly-realistic-v3.12.0.jar` does not use vanilla configured/placed features for wild carrots or potatoes.

It uses datapack functions:

- `data/minecraft/tags/function/load.json` runs `overly_realistic:main/load`.
- `overly_realistic:main/load` runs `overly_realistic:main/data/load_data_0`.
- `load_data_0` detects first-time world setup with `version_id == 0` and runs `overly_realistic:main/world_load_actions/world_load_first_time` at `@p`.
- `world_load_first_time` runs `operate_spawn_animals` as nearby common animals.
- `operate_spawn_animals` has `animal_place_prop_rng` at `0.98`, then summons a marker and calls `general/props/placement/prop_place_from_animal_spawn`.
- `prop_place_from_animal_spawn` uses `spreadplayers`, `positioned over motion_blocking_no_leaves`, then calls `prop_place_tier_2`.
- `prop_place_tier_2` validates the block below with `#overly_realistic:ground_prop_tier_2_can_gen_on`, validates replaceability with `#overly_realistic:spear_can_passthrough`, changes dirt below to `rooted_dirt`, then chooses one of:
  - `setblock ~ ~ ~ carrots[age=7] strict`
  - `setblock ~ ~ ~ potatoes[age=7] strict`
  - `setblock ~ ~ ~ beetroots[age=3] strict`

Relevant tags:

- `ground_prop_tier_2_can_gen_on`: `grass_block`, `dirt`, `coarse_dirt`, `mycelium`, `podzol`, `rooted_dirt`.
- `spear_can_passthrough`: air, water/lava, crops, flowers, grass, bushes, snow, signs, buttons, etc.
- `ground_prop_cant_gen_on`: pass-through blocks, leaves, stairs, slabs, carpets, trapdoors.

For NeoForge, prefer real biome modifiers / placed features instead of this function-spread marker pattern unless intentionally porting datapack behavior.
