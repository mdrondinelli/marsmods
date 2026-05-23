# Design Cosmetic Blood FX

Use this when adapting datapack-style visual damage effects into a small NeoForge mod.

## Server-Shared Cosmetics

For cosmetic effects that should be visible to nearby players, keep spawning server-side:

- Use `LivingDamageEvent.Post` so armor/reduction/cancelled hits are already resolved.
- Require final damage above zero.
- Gate eligible entities through ordered datapack entity type profile tags, not hardcoded Java lists.
- Use common config for broad performance and presentation knobs.

## WOR-Style Ground Splatters

The WOR datapack uses a flat tinted item model on an `item_display` entity. In Java:

- Create `EntityType.ITEM_DISPLAY` on the `ServerLevel`.
- Put the display item in slot `0` using `Display.ItemDisplay#getSlot(0).set(stack)`.
- Set `DataComponents.ITEM_MODEL` on the stack to point at an item definition under `assets/<modid>/items/...`.
- Tag the display entity so cleanup and interaction handlers can identify only this mod's decals.
- Clean decals periodically with `LevelTickEvent.Post`, not every tick.

## Data And Pack Overrides

Resource packs naturally override the bundled item definition/model/texture by namespace and path. Datapacks can tune behavior through profile tags:

- `tags/entity_type/red_blood.json`, `blue_green_blood.json`, and similar profile tags control which entity types emit each blood color.
- If profile tags overlap, Java should resolve them in a deliberate order so special cases like bees or arachnids beat broad defaults.
- `tags/block/blood_cannot_spawn_on.json` blocks ground splatter placement on unsuitable surfaces.

This keeps the Java mod focused on hooks/lifecycle while leaving content decisions pack-friendly.
