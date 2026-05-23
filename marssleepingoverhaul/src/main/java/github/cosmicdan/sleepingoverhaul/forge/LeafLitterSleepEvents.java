package github.cosmicdan.sleepingoverhaul.forge;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

public class LeafLitterSleepEvents {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Level level = player.level();
        BlockPos pos = event.getPos();
        if (!isFullLeafLitter(level.getBlockState(pos))) return;
        if (!hasAdjacentFullLeafLitter(level, pos)) return;

        event.setCanceled(true);
        serverPlayer.startSleepInBed(pos).left().ifPresent(problem ->
            serverPlayer.sendOverlayMessage(problem.message()));
    }

    @SubscribeEvent
    public void onCanPlayerSleep(CanPlayerSleepEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        if (!isFullLeafLitter(event.getState())) return;
        if (!hasAdjacentFullLeafLitter(level, pos)) return;
        event.setProblem(null);
    }

    @SubscribeEvent
    public void onCanContinueSleeping(CanContinueSleepingEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity.getSleepingPos().isEmpty()) return;
        BlockPos pos = entity.getSleepingPos().get();
        Level level = entity.level();
        if (!isFullLeafLitter(level.getBlockState(pos))) return;
        if (!hasAdjacentFullLeafLitter(level, pos)) return;
        event.setContinueSleeping(true);
    }

    @SubscribeEvent
    public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        if (event.getNewSpawn() == null) return;
        Player player = event.getEntity();
        if (isFullLeafLitter(player.level().getBlockState(event.getNewSpawn())))
            event.setCanceled(true);
    }

    private static boolean isFullLeafLitter(BlockState state) {
        return state.is(Blocks.LEAF_LITTER)
            && state.getValue(SegmentableBlock.AMOUNT) == SegmentableBlock.MAX_SEGMENT;
    }

    private static boolean hasAdjacentFullLeafLitter(Level level, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (isFullLeafLitter(level.getBlockState(pos.relative(dir)))) return true;
        }
        return false;
    }
}
