package github.cosmicdan.sleepingoverhaul.server;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.TimelapseKillDamageSource;
import github.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingBouncePacket;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingPacket;
import github.cosmicdan.sleepingoverhaul.networking.TimelapseChangePacket;
import net.minecraft.util.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;


/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ServerState {
    // -1 = inactive
    private long timelapseEnd = -1;

    // these are used for logging only
    private long timelapseStartNanos = 0;
    private long timelapseTickCount = 0;

    public boolean isTimelapseActive() {
        return timelapseEnd > -1;
    }

    public boolean didTickTimelapse(ServerLevel serverLevel, long currentTime, long targetTime) {
        if (timelapseEnd == -1) {
            timelapseEnd = targetTime;
            notifyPlayersTimelapseChange(serverLevel.players(), timelapseEnd);
            onTimelapseStart();
        } else if (currentTime >= timelapseEnd) {
            stopTimelapseNow(serverLevel);
        }
        return timelapseEnd > -1;
    }

    @SubscribeEvent
    public void onServerTickPost(ServerTickEvent.Post event) {
        if ((timelapseEnd > -1) && SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get())
            timelapseTickCount++;
    }

    public void onTimelapseStart() {
        if (SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get()) {
            timelapseStartNanos = Util.getNanos();
            timelapseTickCount = 0;
        }
    }

    public void onTimelapseEnd() {
        if (SleepingOverhaul.serverConfig.logTimelapsePerformanceStats.get()) {
            final double timelapseSeconds = (Util.getNanos() - timelapseStartNanos) / 1000000.0 / 1000.0;
            final double ticksPerSecond = timelapseTickCount / timelapseSeconds;
            SleepingOverhaul.LOGGER.info("Timelapse finished. Average TPS = {}; total time = {} seconds; total ticks = {}", ticksPerSecond, timelapseSeconds, timelapseTickCount);
        }
    }

    public void tryReallySleepingRecv(ReallySleepingPacket packet, Player player) {
        boolean reallySleeping = packet.reallySleeping();
        if (player instanceof ServerPlayer serverPlayer) {
            if (reallySleeping && SleepingOverhaul.serverConfig.bedRestEnabled.get() && SleepingOverhaul.MODPLATFORM.canPlayerStartSleepNow(serverPlayer)) {
                //noinspection CastToIncompatibleInterface
                ((PlayerMixinProxy) player).so2_$setReallySleeping(true);
                ((net.minecraft.server.level.ServerLevel) serverPlayer.level()).updateSleepingPlayerList();
            } else {
                PacketDistributor.sendToPlayer(serverPlayer, new ReallySleepingBouncePacket(false));
            }
        } else {
            SleepingOverhaul.LOGGER.warn("The player instance received from packet is not ServerPlayer, eh? NeoForge changed stuff? Bed rest will be bugged...!");
        }
    }

    private void notifyPlayersTimelapseChange(final Iterable<ServerPlayer> players, final long timelapseEnd) {
        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, new TimelapseChangePacket(timelapseEnd));
        }
    }

    public void stopTimelapseNow(ServerLevel serverLevel) {
        if (isTimelapseActive()) {
            timelapseEnd = -1;
            notifyPlayersTimelapseChange(serverLevel.players(), timelapseEnd);
            onTimelapseEnd();
        }
    }

    public float getPlayerHurtAdj(ServerPlayer player, DamageSource source, float amount) {
        float amountAdjusted = amount;
        if (isTimelapseActive()) {
            if (source.isDirect() && player.isSleeping()) {
                switch (SleepingOverhaul.serverConfig.timelapseSleepersDirectDamageAction.get()) {
                    case NoChange -> {}
                    case InstantKill -> amountAdjusted = Float.POSITIVE_INFINITY;
                    case Invincible -> amountAdjusted = Float.NaN;
                }
            } else if (!player.isSleeping() && SleepingOverhaul.serverConfig.noDamageToNonSleepers.get()) {
                amountAdjusted = Float.NaN;
            }
        }
        return amountAdjusted;
    }

    public boolean shouldPreventLivingTravel() {
        return (isTimelapseActive() && SleepingOverhaul.serverConfig.disableLivingEntityTravel.get());
    }

    public boolean shouldPreventNaturalSpawning() {
        return (isTimelapseActive() && SleepingOverhaul.serverConfig.disableNaturalSpawning.get());
    }

    public void setTimelapseEndForClient(long timelapseEndIn) {
        timelapseEnd = timelapseEndIn;
    }
}
