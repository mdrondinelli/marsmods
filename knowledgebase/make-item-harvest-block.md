# Make An Item Harvest A Block

Use this when an item should harvest a block it normally cannot.

## Working API

Use `PlayerEvent.HarvestCheck`:

```java
@SubscribeEvent
public void allowFlintStoneHarvest(PlayerEvent.HarvestCheck event) {
    if (event.getTargetBlock().is(Blocks.STONE) && event.getEntity().getMainHandItem().is(Items.FLINT)) {
        event.setCanHarvest(true);
    }
}
```

## Project Finding

For flint harvesting vanilla stone, `HarvestCheck` is the important gate. After `Items.FLINT` is allowed to harvest `Blocks.STONE`, vanilla stone loot runs and drops cobblestone.

No custom `BlockDropsEvent` cobblestone branch is needed.

## Related Event

Use `PlayerEvent.BreakSpeed` separately if the new harvesting item should still be slow:

```java
if (event.getEntity().getMainHandItem().is(Items.FLINT) && event.getState().is(Blocks.STONE)) {
    event.setNewSpeed(Math.min(event.getNewSpeed(), 0.3f));
}
```

## What Not To Do

- Do not try to fix a failed harvest path only by changing drops. If harvest fails, normal loot may never be produced.
- Do not replace vanilla drops when vanilla loot already does the right thing after harvest succeeds.
- Do not make broad item/block rules if requirement is exact, e.g. vanilla `Blocks.STONE` only.
