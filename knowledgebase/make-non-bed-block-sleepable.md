# Make A Non-Bed Block Sleepable

Use this when a block starts sleep with `Player#startSleepInBed(BlockPos)` but is not a vanilla `BedBlock`.

## Required Bed Hooks

NeoForge sleep rendering and wake-up placement use `BlockState` bed extension methods after sleep starts:

```java
state.isBed(level, pos, sleeper)
state.setBedOccupied(level, pos, sleeper, occupied)
state.getBedDirection(level, pos)
```

If a non-bed block only calls `startSleepInBed`, the player can enter sleep but vanilla wake-up code will not run bed stand-up placement, and client rendering has no bed orientation. Override these methods on the block, usually with a mixin for vanilla blocks.

## Direction Matters

`ServerPlayer#startSleepInBed(BlockPos)` reads `HorizontalDirectionalBlock.FACING` directly for range and obstruction checks. `LivingEntity#stopSleeping()` then uses bed direction for `BedBlock.findStandUpPosition(...)`.

For two-block bed-like surfaces, store or derive a direction where:

```java
BlockPos head = sleepingPos;
BlockPos foot = head.relative(direction.getOpposite());
```

Before calling `startSleepInBed`, make sure the head block has that `FACING` value, or vanilla physical checks may validate the wrong second block.

## Wake Safety

Reject sleep if either body block is obstructed above, and reject sleep if:

```java
BedBlock.findStandUpPosition(EntityType.PLAYER, level, head, direction, player.getYRot()).isEmpty()
```

This avoids accepting cramped sleep positions that later wake the player inside a block.
