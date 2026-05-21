package github.cosmicdan.sleepingoverhaul.forge.mixin.injection;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

public class FeaturesMixinForge {}

@Mixin(ServerPlayer.class)
abstract class FeaturesMixinsForgeServerPlayer{
    @Shadow
    public abstract ServerLevel serverLevel();

    /**
     * For feature to allow rest/sleep in any dimension.
     * In MC 26.x, natural() was removed; use !hasFixedTime() as equivalent.
     */
    @WrapOperation(
            method = "lambda$startSleepInBed$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/dimension/DimensionType;hasFixedTime()Z")
    )
    private boolean onStartSleepInBedNaturalDimensionCheck(DimensionType instance, Operation<Boolean> original) {
        boolean hasFixedTime = original.call(instance);
        if (hasFixedTime && SleepingOverhaul.serverConfig.featureAllowAnyDimension.get())
            hasFixedTime = false; // pretend it has a day/night cycle
        return hasFixedTime;
    }

    /**
     * For feature to allow setting spawn in any dimension.
     * In MC 26.x, setRespawnPosition signature changed; bedWorks() removed — use !hasFixedTime() proxy.
     */
    @WrapOperation(
            method = "lambda$startSleepInBed$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V")
    )
    private void onStartSleepInBedSetRespawn(ServerPlayer instance, net.minecraft.server.level.ServerPlayer.RespawnConfig config, boolean showMessage, Operation<Void> original) {
        boolean canSetSpawn = !serverLevel().dimensionType().hasFixedTime();
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureSetSpawnAnyDimension.get())
            canSetSpawn = true;
        if (canSetSpawn)
            original.call(instance, config, showMessage);
    }
}
