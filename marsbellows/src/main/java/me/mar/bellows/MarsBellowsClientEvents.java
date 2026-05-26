package me.mar.bellows;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = MarsBellows.MODID, value = Dist.CLIENT)
public final class MarsBellowsClientEvents {
    private MarsBellowsClientEvents() {
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.KILN.get(), KilnScreen::new);
    }
}
