package github.cosmicdan.sleepingoverhaul.server;

import github.cosmicdan.sleepingoverhaul.IClientState;
import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingBouncePacket;
import github.cosmicdan.sleepingoverhaul.networking.TimelapseChangePacket;
import net.minecraft.world.entity.player.Player;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public class ClientStateDummy implements IClientState {

    @Override
    public void recvTimelapseChange(TimelapseChangePacket packet, Player player) {}

    @Override
    public void recvTrySleepBounce(ReallySleepingBouncePacket packet, Player player) {}

    @Override
    public boolean isSleepButtonActive() {
        return false;
    }

    @Override
    public <T> void leaveBedButtonAssign(T button) {}

    @Override
    public <T> void sleepButtonAssign(final T button) {}

    @Override
    public void sleepButtonEnable(boolean enable) {}

    @Override
    public void onClickSleep() {}
}
