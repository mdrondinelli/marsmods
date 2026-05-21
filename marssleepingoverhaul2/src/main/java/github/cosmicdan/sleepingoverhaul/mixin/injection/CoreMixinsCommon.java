package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;
import net.neoforged.neoforge.common.util.ClockAdjustment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public class CoreMixinsCommon {}

@Mixin(ServerLevel.class)
abstract class CoreMixinsCommonServerLevel extends Level {
    protected CoreMixinsCommonServerLevel(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Shadow protected abstract void wakeUpAllPlayers();

    @Shadow protected abstract void resetWeatherCycle();

    /**
     * Intercepts EventHooks.onSleepFinished to control what happens when players sleep.
     * Returns null to suppress clock advancement (timelapse or Nothing mode).
     * currentTime and targetTime are total clock ticks (not day time).
     */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;onSleepFinished(Lnet/minecraft/server/level/ServerLevel;Lnet/neoforged/neoforge/common/util/ClockAdjustment;)Lnet/neoforged/neoforge/common/util/ClockAdjustment;", remap = false)
    )
    private ClockAdjustment onSleepFinished(ServerLevel instance, ClockAdjustment defaultAdjustment, Operation<ClockAdjustment> original) {
        switch (SleepingOverhaul.serverConfig.sleepAction.get()) {
            case Timelapse -> {
                final long currentTotalTicks = instance.getDefaultClockTime();
                // Target = start of next day (next multiple of 24000 in total ticks)
                final long targetTotalTicks = ((currentTotalTicks / 24000L) + 1L) * 24000L;
                final boolean hasTimelapseStopped = !SleepingOverhaul.serverState.didTickTimelapse(instance, currentTotalTicks, targetTotalTicks);
                if (hasTimelapseStopped) {
                    wakeUpAllPlayers();
                    so2_$resetWeatherCycleIfNeeded();
                }
                return null; // suppress vanilla clock advancement; normal tick-based time still runs
            }
            case SkipTime -> {
                ClockAdjustment result = original.call(instance, defaultAdjustment);
                wakeUpAllPlayers();
                so2_$resetWeatherCycleIfNeeded();
                return result;
            }
            case Nothing -> {
                return null; // suppress vanilla clock advancement, players stay in bed until morning
            }
        }
        return original.call(instance, defaultAdjustment);
    }

    @Unique
    public void so2_$resetWeatherCycleIfNeeded() {
        if (((ServerLevel)(Object)this).getGameRules().get(GameRules.ADVANCE_WEATHER) && isRaining() && SleepingOverhaul.serverConfig.resetWeatherOnWake.get())
            resetWeatherCycle();
    }

    /**
     * Suppress vanilla wakeUpAllPlayers() in tick(); we call it ourselves at the right time.
     */
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;wakeUpAllPlayers()V"),
            require = 1, allow = 1
    )
    public final void onWakeUpAllPlayers(ServerLevel self) {}

    /**
     * Suppress vanilla resetWeatherCycle() after sleep in tick(); we handle weather ourselves.
     */
    @Redirect(
            method = "tick(Ljava/util/function/BooleanSupplier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;resetWeatherCycle()V"),
            require = 1, allow = 1
    )
    public final void onResetWeatherCycle(ServerLevel self) {}
}
