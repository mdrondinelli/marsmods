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

For flint harvesting logs, use the same `HarvestCheck` event with `BlockTags.LOGS` instead of enumerating log blocks:

```java
if (event.getTargetBlock().is(BlockTags.LOGS)) {
    event.setCanHarvest(isAllowedLogHarvester(event.getEntity().getMainHandItem()));
}
```

Logs do not have `requiresCorrectToolForDrops()`, so `incorrect_for_wooden_tool` alone cannot stop log drops. Use `HarvestCheck` and `BlockDropsEvent` to make logs drop only for flint or non-wood axes.

Use `BlockDropsEvent` for post-success flint wear on logs. `Player#getRandom().nextInt(6) == 0` gives a one-in-six break chance:

```java
if (event.getState().is(BlockTags.LOGS)
        && event.getBreaker() instanceof Player player
        && player.getMainHandItem().is(Items.FLINT)
        && player.getRandom().nextInt(6) == 0) {
    player.getMainHandItem().consume(1, player);
    player.onEquippedItemBroken(Items.FLINT, EquipmentSlot.MAINHAND);
}
```

## Related Event

Use `PlayerEvent.BreakSpeed` separately if the new harvesting item should still be slow:

```java
if (event.getEntity().getMainHandItem().is(Items.FLINT) && event.getState().is(Blocks.STONE)) {
    event.setNewSpeed(Math.min(event.getNewSpeed(), 0.3f));
}
```

Use `BlockDropsEvent` for post-success effects after the block actually breaks. For a consumable non-tool item such as flint, consume the item and trigger vanilla break feedback:

```java
if (event.getState().is(Blocks.STONE)
        && event.getBreaker() instanceof Player player
        && player.getMainHandItem().is(Items.FLINT)) {
    player.getMainHandItem().consume(1, player);
    player.onEquippedItemBroken(Items.FLINT, EquipmentSlot.MAINHAND);
}
```

`ItemStack#consume` respects creative/infinite-materials behavior. `Player#onEquippedItemBroken` broadcasts the vanilla item break sound/particles for the equipment slot.

## Incorrect Tool Tags

Vanilla tool materials use block tags such as `minecraft:incorrect_for_wooden_tool` to decide when a material is not correct for drops. Adding blocks to this tag helps vanilla harvest checks and overlay mods that inspect vanilla tool correctness.

This only matters when the block requires a correct tool. For logs, enforce the rule with events because logs normally drop even when the tool is not correct.

## Actual Multi-Role Tools

In Minecraft/NeoForge `26.1.2`, `ToolMaterial#applyToolProperties` takes one efficient block tag. For an item that should behave like two vanilla tool classes, such as an axe plus a pickaxe, either define a custom tool component directly or override `Item#getDestroySpeed` and `Item#isCorrectToolForDrops` for the second role. Extending `AxeItem` keeps axe right-click actions such as stripping available.

## What Not To Do

- Do not try to fix a failed harvest path only by changing drops. If harvest fails, normal loot may never be produced.
- Do not replace vanilla drops when vanilla loot already does the right thing after harvest succeeds.
- Do not make broad item/block rules if requirement is exact, e.g. vanilla `Blocks.STONE` only.
