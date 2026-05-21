package github.cosmicdan.sleepingoverhaul.mixin.injection;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.client.ClientConfig;
import github.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import github.cosmicdan.sleepingoverhaul.server.ServerConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class TimelapseMixinsCommonClient {}

@Mixin(Camera.class)
abstract class TimelapseMixinsCommonClientCamera {
    @Shadow
    protected abstract void move(float d, float e, float f);

    @Shadow protected abstract void setRotation(float f, float g);

    @Shadow public abstract BlockPos blockPosition();

    @Shadow protected abstract void setPosition(Vec3 vec3);

    @Shadow public abstract Vec3 position();

    @Shadow public abstract Entity entity();

    @Shadow protected abstract void disablePanoramicMode();

    private double previousMaxHeight = -1.0;

    /**
     * For cinematic camera during Timelapse. Hooks Camera.update() instead of removed setup().
     */
    @Inject(
            method = "update",
            at = @At("TAIL"),
            require = 1, allow = 1
    )
    private void afterCameraUpdate(DeltaTracker deltaTracker, CallbackInfo ci) {
        Entity entity = entity();
        if (entity instanceof Player player) {
            final int cineStage = SleepingOverhaul.clientState.getTimelapseCinematicStage();
            if (cineStage == 1) {
                disablePanoramicMode();
                SleepingOverhaul.clientState.advanceTimelapseCinematicStage();
                previousMaxHeight = -1.0;
            } else if (cineStage == 2 && ((PlayerMixinProxy) player).so2_$isReallySleeping()) {
                final ClientConfig.TimelapseCameraType timelapseCameraType = SleepingOverhaul.clientConfig.timelapseCameraType.get();
                if ((timelapseCameraType != ClientConfig.TimelapseCameraType.None) && (player.level() instanceof ClientLevel level)) {
                    final float timeOfDayAsFraction = (level.getDefaultClockTime() % 24000L) / 24000.0f;

                    setRotation((timeOfDayAsFraction * 360.0f * 2.0f) - 90, 0.0f);

                    if (timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceOrbit) {
                        move(-6.0f, 1.0f, 0.0f);
                    }
                    if ((timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceOrbit) || (timelapseCameraType == ClientConfig.TimelapseCameraType.SurfaceRotation)) {
                        final BlockPos topmostPosition = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, blockPosition()).above(3);
                        if (topmostPosition.getY() > previousMaxHeight)
                            previousMaxHeight = topmostPosition.getY();
                        setPosition(new Vec3(position().x(), previousMaxHeight, position().z()));
                    }
                }
            } else if (cineStage == 3) {
                previousMaxHeight = -1.0;
                disablePanoramicMode();
                SleepingOverhaul.clientState.advanceTimelapseCinematicStage();
            }
        }
    }
}

@Mixin(Gui.class)
abstract class TimelapseMixinsCommonClientGui {

    /**
     * For cinematic camera during Timelapse, use user-configured screen dim value.
     */
    @WrapOperation(
            method = "extractSleepOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I")
    )
    private int onRenderGetSleepTimer(LocalPlayer instance, Operation<Integer> original) {
        if (SleepingOverhaul.serverConfig.sleepAction.get() == ServerConfig.SleepAction.Timelapse) {
            if (SleepingOverhaul.clientState.isTimelapseCinematicActive()) {
                return SleepingOverhaul.clientConfig.timelapseDimValue.get();
            }
        }
        return original.call(instance);
    }
}

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
