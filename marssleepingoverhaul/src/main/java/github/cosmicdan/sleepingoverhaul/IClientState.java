package github.cosmicdan.sleepingoverhaul;

import github.cosmicdan.sleepingoverhaul.networking.ReallySleepingBouncePacket;
import github.cosmicdan.sleepingoverhaul.networking.TimelapseChangePacket;
import net.minecraft.world.entity.player.Player;

/**
 * @author Daniel 'CosmicDan' Connolly
 */
public interface IClientState {
    boolean isSleepButtonActive();

    <T> void leaveBedButtonAssign(T buttonRaw);

    <T> void sleepButtonAssign(T buttonRaw);

    void sleepButtonEnable(boolean enable);

    void onClickSleep();

    void recvTimelapseChange(TimelapseChangePacket packet, Player player);

    void recvTrySleepBounce(ReallySleepingBouncePacket packet, Player player);
}
