package github.cosmicdan.sleepingoverhaul;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class LeafLitterSleepSupport {
    private LeafLitterSleepSupport() {}

    public static boolean isFullLeafLitter(BlockState state) {
        return state.is(Blocks.LEAF_LITTER)
            && state.getValue(SegmentableBlock.AMOUNT) == SegmentableBlock.MAX_SEGMENT;
    }

    public static Optional<Direction> getSleepDirection(BlockGetter level, BlockPos headPos, BlockState headState) {
        if (!isFullLeafLitter(headState)) return Optional.empty();

        Direction stateDirection = headState.getValue(HorizontalDirectionalBlock.FACING);
        if (isFullLeafLitter(level.getBlockState(headPos.relative(stateDirection.getOpposite())))) {
            return Optional.of(stateDirection);
        }

        for (Direction adjacentDirection : Direction.Plane.HORIZONTAL) {
            if (isFullLeafLitter(level.getBlockState(headPos.relative(adjacentDirection)))) {
                return Optional.of(adjacentDirection.getOpposite());
            }
        }

        return Optional.empty();
    }

    public static boolean canSleepOn(Level level, BlockPos headPos, Player player) {
        BlockState headState = level.getBlockState(headPos);
        Optional<Direction> sleepDirection = getSleepDirection(level, headPos, headState);
        return sleepDirection.isPresent()
            && bodySpaceClear(level, headPos, sleepDirection.get())
            && hasStandUpPosition(level, headPos, sleepDirection.get(), player);
    }

    public static boolean prepareForSleep(Level level, BlockPos headPos, Player player) {
        BlockState headState = level.getBlockState(headPos);
        Optional<Direction> sleepDirection = getSleepDirection(level, headPos, headState);
        if (sleepDirection.isEmpty()) return false;

        Direction direction = sleepDirection.get();
        BlockState orientedHeadState = headState.setValue(HorizontalDirectionalBlock.FACING, direction);
        if (orientedHeadState != headState) {
            level.setBlock(headPos, orientedHeadState, 3);
        }

        return bodySpaceClear(level, headPos, direction) && hasStandUpPosition(level, headPos, direction, player);
    }

    public static Direction getBedDirection(BlockGetter level, BlockPos headPos, BlockState headState) {
        return getSleepDirection(level, headPos, headState).orElse(headState.getValue(HorizontalDirectionalBlock.FACING));
    }

    private static boolean bodySpaceClear(CollisionGetter level, BlockPos headPos, Direction direction) {
        return isFree(level, headPos.above()) && isFree(level, headPos.above().relative(direction.getOpposite()));
    }

    private static boolean isFree(CollisionGetter level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    private static boolean hasStandUpPosition(CollisionGetter level, BlockPos headPos, Direction direction, Player player) {
        Optional<Vec3> standUpPosition = BedBlock.findStandUpPosition(EntityType.PLAYER, level, headPos, direction, player.getYRot());
        return standUpPosition.isPresent();
    }
}
