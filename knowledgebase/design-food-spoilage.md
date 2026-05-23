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
- To apply spoiled-food consequences, use `LivingEntityUseItemEvent.Finish`; the event item is the pre-consumption copy, so it can still be checked for freshness data after vanilla consumes the stack.
- Load additive rule files from `data/*/spoilage_rules/*.json`. Keep spoilage profiles and spoiled-food effect rules as separate ordered tag-match lists. Scan higher-priority packs before lower-priority packs and preserve front-to-back order within each file, so specific rules can be placed before broad fallback tags.
- Treat `marsfoodspoilage:does_not_spoil` as a hard-coded exclusion tag before rule matching, not as a normal spoilage rule; it must override broader food tags.
- `ItemTooltipEvent` only changes inventory tooltip lines. The hotbar selected-item popup uses `ItemStack#getHoverName` / `getHighlightTip`, so freshness in that popup needs a client mixin or equivalent hook on the highlight path.
- To reduce active-use interruptions and inventory churn, touch logic should write freshness back only when the computed freshness state changes, not on every elapsed-time update. Tooltips and eating logic can compute exact freshness lazily from the stored timestamp.
- For stale/spoiled eat slowdown, change the stack `DataComponents.CONSUMABLE.consume_seconds` when freshness state changes. Keep multipliers in common config so packs can tune the penalties. Event-only use-duration changes do not stretch first-person eat animation or repeated eat sounds.

## Container Notes

Unloaded containers do not need active ticking. When a player opens a container, sweep the menu slots and apply elapsed time from the freshness component. Item-backed containers can be handled by reading and writing `DataComponents.CONTAINER` / `ItemContainerContents`.

`ExplosionEvent.Detonate` is not a good v1 spoilage hook. It is early and indirect; item entity and pickup boundaries cover observable dropped stacks.

## What Not To Do

- Do not use wall-clock time unless spoilage should advance while the server is stopped.
- Do not globally scan loaded chunks or block entities for food.
- Do not change the item ID for spoiled food if downstream recipes or identity-sensitive systems should still see the original item.
