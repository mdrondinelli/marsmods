# Add A Facing Supported Block

Use this when a block needs four horizontal directions and should break when support below is removed.

## State

For Minecraft `26.1.2`, use `EnumProperty<Direction>` with `BlockStateProperties.HORIZONTAL_FACING`.

Register the default state in the constructor:

```java
this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
```

Add `FACING` in `createBlockStateDefinition`.

## Placement

To make the model front face the player when placed:

```java
return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
```

## Support Below

Require a sturdy upward face from the block below:

```java
BlockPos below = pos.below();
return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
```

In `updateShape`, return air when the downward neighbor changes and the block can no longer survive.

## Assets

Blockstate variants should include all four facing values and rotate the same model by `y`: `0`, `90`, `180`, and `270`.
