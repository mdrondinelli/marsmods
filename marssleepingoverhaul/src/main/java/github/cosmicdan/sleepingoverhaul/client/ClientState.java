package github.cosmicdan.sleepingoverhaul.client;

import github.cosmicdan.sleepingoverhaul.IClientState;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import github.cosmicdan.sleepingoverhaul.mixin.proxy.PlayerMixinProxy;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingBouncePacket;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingPacket;
import github.cosmicdan.sleepingoverhaul.networking.TimelapseChangePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientState implements IClientState {
    private Button leaveButton = null;
    private Button sleepButton = null;

    @Override
    public void recvTimelapseChange(TimelapseChangePacket packet, Player player) {
        final long timelapseEnd = packet.timelapseEnd();
        SleepingOverhaul.serverState.setTimelapseEndForClient(timelapseEnd);
    }

    @Override
    public void recvTrySleepBounce(ReallySleepingBouncePacket packet, Player player) {
        if (!packet.reallySleeping()) {
            player.sendOverlayMessage(Component.translatable("gui.sleepingoverhaul.sleepNotPossibleNow"));
            ((PlayerMixinProxy) player).so2_$setReallySleeping(false);
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    sleepButtonEnable(true);
                }
            }, 2000);
        }
    }

    @Override
    public boolean isSleepButtonActive() {
        return (sleepButton != null) && sleepButton.isActive();
    }

    @Override
    public <T> void leaveBedButtonAssign(final T buttonRaw) {
        if (buttonRaw instanceof Button button) {
            leaveButton = button;
        }
    }

    @Override
    public <T> void sleepButtonAssign(final T buttonRaw) {
        if (buttonRaw instanceof Button button) {
            sleepButton = button;
        }
    }

    @Override
    public void sleepButtonEnable(boolean enable) {
        if (sleepButton != null)
            sleepButton.active = enable;
    }

    @Override
    public void onClickSleep() {
        if (isSleepButtonActive()) {
            SleepingOverhaul.clientState.sleepButtonEnable(false);
            final LocalPlayer player = Minecraft.getInstance().player;
            ((PlayerMixinProxy) player).so2_$setReallySleeping(true);
            ClientPacketDistributor.sendToServer(new ReallySleepingPacket(true));
        }
    }
}
