package me.mar.compat;

import com.seibel.distanthorizons.api.DhApi;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@Mod("marscompat")
public class MarsCompat {
    private Boolean previousDhEnabled = null;

    public MarsCompat(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(this::onClientTickPost);
    }

    private void onClientTickPost(ClientTickEvent.Post event) {
        boolean isActive = SleepingOverhaul.clientState.isTimelapseCinematicActive();

        if (isActive && previousDhEnabled == null) {
            previousDhEnabled = DhApi.Delayed.configs.graphics().renderingEnabled().getValue();
            DhApi.Delayed.configs.graphics().renderingEnabled().setValue(false);
        } else if (!isActive && previousDhEnabled != null) {
            DhApi.Delayed.configs.graphics().renderingEnabled().setValue(previousDhEnabled);
            previousDhEnabled = null;
        }
    }
}
