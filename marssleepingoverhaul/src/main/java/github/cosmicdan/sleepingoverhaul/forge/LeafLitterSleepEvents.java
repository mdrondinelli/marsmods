package github.cosmicdan.sleepingoverhaul.forge;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.LeafLitterSleepSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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
        if (!LeafLitterSleepSupport.isFullLeafLitter(level.getBlockState(pos))) return;
        if (LeafLitterSleepSupport.getSleepDirection(level, pos, level.getBlockState(pos)).isEmpty()) return;

        event.setCanceled(true);
        if (!LeafLitterSleepSupport.prepareForSleep(level, pos, player)) {
            serverPlayer.sendOverlayMessage(Player.BedSleepingProblem.OBSTRUCTED.message());
            return;
        }

        serverPlayer.startSleepInBed(pos).left().ifPresent(problem ->
            serverPlayer.sendOverlayMessage(problem.message()));
    }

    @SubscribeEvent
    public void onCanPlayerSleep(CanPlayerSleepEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        if (!LeafLitterSleepSupport.isFullLeafLitter(event.getState())) return;
        event.setProblem(LeafLitterSleepSupport.canSleepOn(level, pos, event.getEntity())
            ? null
            : Player.BedSleepingProblem.OBSTRUCTED);
    }

    @SubscribeEvent
    public void onCanContinueSleeping(CanContinueSleepingEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        LivingEntity entity = event.getEntity();
        if (entity.getSleepingPos().isEmpty()) return;
        BlockPos pos = entity.getSleepingPos().get();
        Level level = entity.level();
        if (!LeafLitterSleepSupport.isFullLeafLitter(level.getBlockState(pos))) return;
        if (LeafLitterSleepSupport.getSleepDirection(level, pos, level.getBlockState(pos)).isEmpty()) return;
        event.setContinueSleeping(true);
    }

    @SubscribeEvent
    public void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!SleepingOverhaul.serverConfig.leafLitterSleepEnabled.get()) return;
        if (event.getNewSpawn() == null) return;
        Player player = event.getEntity();
        if (LeafLitterSleepSupport.isFullLeafLitter(player.level().getBlockState(event.getNewSpawn())))
            event.setCanceled(true);
    }
}
