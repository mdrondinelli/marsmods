package github.cosmicdan.sleepingoverhaul.forge.mixin.injection;

import com.mojang.datafixers.util.Either;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import github.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

public class BedRestMixinsForge {}

@Mixin(ServerPlayer.class)
abstract class BedRestMixinsForgeServerPlayer {

    /**
     * For Bed Rest; remove canPlayerStartSleeping check on ServerPlayer's startSleepInBed, but only if the reason was NOT_POSSIBLE_NOW (day time check). We perform the check later in ServerState#onReallySleepingRecv
     */
    @WrapOperation(
            method = "startSleepInBed",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;canPlayerStartSleeping(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/core/BlockPos;Lcom/mojang/datafixers/util/Either;)Lcom/mojang/datafixers/util/Either;", remap = false)
    )
    private Either<Player.BedSleepingProblem, Unit> onCanPlayerStartSleepingEvent(ServerPlayer player, BlockPos pos, Either<Player.BedSleepingProblem, Unit> vanillaResult, Operation<Either<Player.BedSleepingProblem, Unit>> original) {
        if (vanillaResult.left().isPresent() && SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
            Player.BedSleepingProblem problem = vanillaResult.left().get();
            // Only override time/environment-based blocks; never override physical constraints
            boolean isPhysicalConstraint = problem == Player.BedSleepingProblem.TOO_FAR_AWAY
                || problem == Player.BedSleepingProblem.OBSTRUCTED
                || problem == Player.BedSleepingProblem.NOT_SAFE
                || problem == Player.BedSleepingProblem.OTHER_PROBLEM;
            if (!isPhysicalConstraint) {
                return Either.right(Unit.INSTANCE);
            }
        }
        return original.call(player, pos, vanillaResult);
    }
}

@Mixin(Player.class)
abstract class BedRestMixinsForgePlayer {

    /**
     * For Bed Rest; remove canEntityContinueSleeping check during tick but only if they're not reallySleeping.
     */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;canEntityContinueSleeping(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/player/Player$BedSleepingProblem;)Z", remap = false)
    )
    private boolean onTimeCheck(LivingEntity sleeper, Player.BedSleepingProblem problem, Operation<Boolean> original) {
        if (sleeper instanceof Player player && SleepingOverhaul.serverConfig.bedRestEnabled.get()) {
            if (!((PlayerMixinProxy) player).so2_$isReallySleeping())
                return true;
        }
        return original.call(sleeper, problem);
    }
}
