# Blood Trail Implementation — wilos-overly-realistic v3.12.0

Pure datapack mod (no Java). All logic in mcfunctions.

## Summary

Two mechanisms: floating **particles** (ephemeral) and persistent **ground splatter** (`item_display` entities).

---

## 1. Particles

`particle block{block_state:"minecraft:redstone_block"}` — red block-break particles. Used in three places:

- **On hit** (`player_damaged_direct.mcfunction`): burst at player position on damage
- **While bleeding** (`player_bleed.mcfunction`): continuous drip every 20t while `BleedingTimer > 0`
- **While downed** (`downing_blood_time.mcfunction`): every 10t — one burst at feet, one at eye level (`anchored eyes`)

---

## 2. Ground Blood Splatter

### Entity

`item_display` summoned at hit position, translated `y+0.010` to avoid z-fighting:

```
summon minecraft:item_display ~ ~0.5 ~ {
  Tags: ["OverlyRealistic.GroundBloodSplatter", "OverlyRealistic.GroundMarking", ...],
  item: { id: "minecraft:poisonous_potato", components: { "minecraft:item_model": "overlyrealistic:blocks/ground_markings/blood_splatter/blood_splatter_0" } },
  transformation: { scale: [1,1,1], translation: [0, 0.010, 0], ... }
}
```

### Model

Flat 16×16 element (`from [0,0,0]` to `[16,0,16]`), single up-facing face with `tintindex: 0` for color tinting. Texture is 16×16 PNG (grayscale+alpha — tint provides color).

### Lifecycle

Every **21t**, all tagged ground markings run `ground_marking_operate`:
1. Increment timer scoreboard
2. Kill if `timer >= max_ground_markings_time` (configurable setting)
3. Kill if no block at `~ ~-0.6 ~` (fell into void / block removed)

Default max time is configurable via `set_max_ground_blood_time.mcfunction`.

### Guards before spawning

Both player and animal paths check:
- Setting `generate_blood_splatters = 1` (toggle)
- Entity is on ground (`IsOnGround` predicate/score)
- Block below is not in `#overly_realistic:ground_markings_cant_generate_on` tag

---

## 3. Bleeding State (Players)

Triggered in `start_bleeding.mcfunction`:
- Sets `BleedingTimer` to `random value 10..180` ticks

Each **20t** tick while `BleedingTimer > 0` (`player_bleed.mcfunction`):
- Decrements timer
- Spawns ground splatter if on ground
- Spawns particle drip
- Calls `#overly_realistic:mechanics/player_currently_bleeding` function tag (extension point)

Bleeding trigger: `player_damaged_direct` → RNG check (`direct_damage_bleeding_rng_small` predicate) → `start_bleeding`. Armor on player makes it harder to trigger.

---

## 4. Animal Blood

`animal_damaged.mcfunction` — fires when animal is hit:
- Checks attacker has tag `OverlyRealistic.Player.PlayerAttackedEntity`
- If entity on ground and setting enabled → `create_blood_splatter`

No persistent bleeding state for animals — single splatter per hit only.

---

## 5. Brush Interaction

Players can "clean" blood splatters by brushing them (`blood_brushed.mcfunction`):
- Plays honey-slide sound + particles
- RNG check (`blood_splatter_brush_rng` predicate) — fail = no remove
- On pass: `kill @s` (removes the `item_display`)

---

## 6. Downstream Effects

Blood splatters affect the **hygiene system**: `main_loop_99t_operate` checks for any `OverlyRealistic.GroundBloodSplatter` entity within 10 blocks and adds +15 to the hygiene-degradation score for the nearby player.

---

## Key Takeaways for Adaptation

| Mechanic | Implementation |
|----------|----------------|
| Visual blood decal | `item_display` + flat Blockbench model + `tintindex` for color |
| Decay timer | Per-entity scoreboard + periodic `execute as @e[tag=...]` loop |
| Trigger hook | `#overly_realistic:mechanics/player_currently_bleeding` function tag |
| Configurable lifespan | Scoreboard value set by `set_max_ground_blood_time` macro function |
| Cleanup guard | Check block below before spawning; check block below in operate loop |
