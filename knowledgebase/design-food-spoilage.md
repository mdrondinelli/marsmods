# Design Food Spoilage

Use item data components for per-stack freshness. In Minecraft/NeoForge 26.1.2, item stack custom data should live in a registered `DataComponentType`, not an item attachment.

## Working Pattern

- Store freshness metadata on `ItemStack` with a persistent, network-synced data component.
- Use an item `DataMapType` for spoilage profiles so packs can configure shelf life by item ID or item tag.
- Compute freshness lazily from `ServerLevel#getGameTime()` and a stored `last_update_tick`; do not scan unloaded chunks or every block entity.
- Touch stacks at observable boundaries:
  - player inventory sparse tick
  - opened container menu slots
  - item entities joining the level
  - loaded item entity sparse tick
  - item pickup pre-event
  - item use start before eating
- If food must not stack, use `ModifyDefaultComponentsEvent` on the mod event bus and set `DataComponents.MAX_STACK_SIZE` to `1` for items with `DataComponents.FOOD`.

## Container Notes

Unloaded containers do not need active ticking. When a player opens a container, sweep the menu slots and apply elapsed time from the freshness component. Item-backed containers can be handled by reading and writing `DataComponents.CONTAINER` / `ItemContainerContents`.

`ExplosionEvent.Detonate` is not a good v1 spoilage hook. It is early and indirect; item entity and pickup boundaries cover observable dropped stacks.

## What Not To Do

- Do not use wall-clock time unless spoilage should advance while the server is stopped.
- Do not globally scan loaded chunks or block entities for food.
- Do not change the item ID for spoiled food if downstream recipes or identity-sensitive systems should still see the original item.
