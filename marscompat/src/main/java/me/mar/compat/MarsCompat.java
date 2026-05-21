package me.mar.compat;

import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("marscompat")
public class MarsCompat {

    public MarsCompat(IEventBus modBus) {
        DhApiEventRegister.on(DhApiBeforeRenderEvent.class, new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
                if (SleepingOverhaul.clientState.isTimelapseCinematicActive()) {
                    event.cancelEvent();
                }
            }
        });
    }
}
