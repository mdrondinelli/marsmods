package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class FeaturesMixinsCommon {}

@Mixin(BedBlock.class)
abstract class FeaturesMixinsCommonBedBlock {
    /**
     * For feature to allow rest/sleep in any dimension.
     * MC 26.x: canSetSpawn(Level) removed; explosion check is now BedRule.explodes().
     */
    @WrapOperation(
            method = "useWithoutItem",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;explodes()Z")
    )
    private boolean onUseExplodes(BedRule bedRule, Operation<Boolean> original) {
        boolean explodes = original.call(bedRule);
        if (explodes && SleepingOverhaul.serverConfig.featureAllowAnyDimension.get())
            return false;
        return explodes;
    }
}

@Mixin(ServerPlayer.class)
abstract class FeaturesMixinsCommonServerPlayer {
    /**
     * For featureAllowAnyDimension: allow sleeping in any dimension.
     * MC 26.x: sleep permission is BedRule.canSleep(Level) inside lambda$startSleepInBed$0.
     */
    @WrapOperation(
            method = "lambda$startSleepInBed$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSleep(Lnet/minecraft/world/level/Level;)Z")
    )
    private boolean onStartSleepCanSleep(BedRule bedRule, Level level, Operation<Boolean> original) {
        boolean canSleep = original.call(bedRule, level);
        if (!canSleep && SleepingOverhaul.serverConfig.featureAllowAnyDimension.get())
            canSleep = true;
        return canSleep;
    }

    /**
     * For featureSetSpawnAnyDimension: allow setting spawn in any dimension (during sleep).
     * MC 26.x: spawn check is BedRule.canSetSpawn(Level) inside lambda$startSleepInBed$0.
     */
    @WrapOperation(
            method = "lambda$startSleepInBed$0",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSetSpawn(Lnet/minecraft/world/level/Level;)Z")
    )
    private boolean onStartSleepCanSetSpawn(BedRule bedRule, Level level, Operation<Boolean> original) {
        boolean canSetSpawn = original.call(bedRule, level);
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureSetSpawnAnyDimension.get())
            canSetSpawn = true;
        return canSetSpawn;
    }

    /**
     * For featureSetSpawnAnyDimension: allow respawning at bed in any dimension.
     * MC 26.x: respawn check is BedRule.canSetSpawn(Level) in findRespawnAndUseSpawnBlock.
     */
    @WrapOperation(
            method = "findRespawnAndUseSpawnBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/attribute/BedRule;canSetSpawn(Lnet/minecraft/world/level/Level;)Z")
    )
    private static boolean onFindRespawnCanSetSpawn(BedRule bedRule, Level level, Operation<Boolean> original) {
        boolean canSetSpawn = original.call(bedRule, level);
        if (!canSetSpawn && SleepingOverhaul.serverConfig.featureSetSpawnAnyDimension.get())
            canSetSpawn = true;
        return canSetSpawn;
    }
}
