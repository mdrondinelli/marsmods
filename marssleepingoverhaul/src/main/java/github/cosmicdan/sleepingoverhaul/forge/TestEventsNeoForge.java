package github.cosmicdan.sleepingoverhaul.forge;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.CanContinueSleepingEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;

public class TestEventsNeoForge {
    public static void subTestEvents() {
        NeoForge.EVENT_BUS.addListener(TestEventsNeoForge::onSleepFinishedTime);
        NeoForge.EVENT_BUS.addListener(TestEventsNeoForge::onPlayerWakeUp);
        NeoForge.EVENT_BUS.addListener(TestEventsNeoForge::onPlayerSetSpawn);
        NeoForge.EVENT_BUS.addListener(TestEventsNeoForge::onCanContinueSleeping);
        NeoForge.EVENT_BUS.addListener(TestEventsNeoForge::onCanPlayerSleep);
    }

    private static void onSleepFinishedTime(SleepFinishedTimeEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: SleepFinishedTimeEvent");
    }

    private static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: PlayerWakeUpEvent");
    }

    private static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: PlayerSetSpawnEvent");
    }

    private static void onCanContinueSleeping(CanContinueSleepingEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: CanContinueSleepingEvent");
    }

    private static void onCanPlayerSleep(CanPlayerSleepEvent event) {
        SleepingOverhaul.LOGGER.info("~ Fired: CanPlayerSleepEvent");
    }
}
