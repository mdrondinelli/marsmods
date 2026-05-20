package me.mar.flinttool;

import net.minecraft.client.Minecraft;
import net.minecraft.client.tutorial.TutorialSteps;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.lifecycle.ClientStartedEvent;
import net.neoforged.neoforge.common.NeoForge;

public class PrimitiveProgressionClientEvents {
    public static void register() {
        NeoForge.EVENT_BUS.register(new PrimitiveProgressionClientEvents());
    }

    @SubscribeEvent
    public void disableVanillaTutorial(ClientStartedEvent event) {
        Minecraft minecraft = event.getClient();
        if (minecraft.options.tutorialStep != TutorialSteps.NONE) {
            minecraft.options.tutorialStep = TutorialSteps.NONE;
            minecraft.options.save();
        }
    }
}
