package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

public class TimelapseMixinsCommonClient {}

@Mixin(LocalPlayer.class)
abstract class TimelapseMixinsCommonClientLocalPlayer {

    @Shadow
    public ClientInput input;

    /**
     * For option to prevent player movement during timelapse
     */
    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V")
    )
    private void onTickInput(ClientInput instance, Operation<Void> original) {
        if (SleepingOverhaul.serverState.isTimelapseActive() && SleepingOverhaul.serverConfig.noMovementDuringTimelapse.get()) {
            input.keyPresses = Input.EMPTY;
        } else {
            original.call(instance);
        }
    }
}
