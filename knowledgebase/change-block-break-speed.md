# Change Block Break Speed

Use `PlayerEvent.BreakSpeed` on the NeoForge event bus.

## Working API

```java
@SubscribeEvent
public void slowHandLogBreaking(PlayerEvent.BreakSpeed event) {
    if (event.getEntity().getMainHandItem().isEmpty() && event.getState().is(BlockTags.LOGS)) {
        event.setNewSpeed(Math.min(event.getNewSpeed(), 0.05f));
    }
}
```

Use `Math.min` when you only want to cap speed downward and avoid accidentally speeding up another mod/tool.

This is event-driven and server-authoritative enough for the current gameplay rule. Keep it as an event handler rather than polling blocks or relying on client presentation.

## Project Examples

Hand-breaking logs:

- condition: empty main hand
- target: `BlockTags.LOGS`
- speed cap: `0.05f`

Flint breaking vanilla stone:

- condition: main hand `Items.FLINT`
- target: `Blocks.STONE`
- speed cap: `0.3f`

Flint breaking logs:

- condition: main hand `Items.FLINT`
- target: `BlockTags.LOGS`
- speed cap: `0.3f`

Stone axe and pickaxe nerf:

- condition: main hand `Items.STONE_AXE` or `Items.STONE_PICKAXE`
- action: multiply final event speed by `0.5f`
- reason: buffs and debuffs still matter because the speed is scaled instead of capped

## What Not To Do

- Do not scan blocks or chunks every tick to slow mining.
- Do not put gameplay authority in client/rendering code.
- Do not enumerate modded logs by ID. Use `BlockTags.LOGS`.
- Do not assume break speed controls drops. Harvest and drops are separate paths.
