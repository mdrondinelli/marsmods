package me.mar.foodspoilage;

import me.mar.dryingrack.DryingRackEvents;
import net.neoforged.bus.api.SubscribeEvent;

public class DryingRackSpoilageBridge {

    @SubscribeEvent
    public void onPlaceCheck(DryingRackEvents.PlaceCheck event) {
        if (SpoilageService.getState(event.getStack(), event.getLevel().getGameTime()) != SpoilageState.FRESH) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEnter(DryingRackEvents.Enter event) {
        SpoilageService.enterRack(event.getLevel(), event.getStack());
    }

    @SubscribeEvent
    public void onExit(DryingRackEvents.Exit event) {
        SpoilageService.exitRack(event.getLevel(), event.getStack());
    }
}
