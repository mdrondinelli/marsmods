package github.cosmicdan.sleepingoverhaul.forge;

import github.cosmicdan.sleepingoverhaul.SleepingOverhaul;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(SleepingOverhaul.MOD_ID)
public class SleepingOverhaulNeoForge {
    private final SleepingOverhaul INSTANCE;
    public static ModContainer CONTAINER;

    public SleepingOverhaulNeoForge(ModContainer container, IEventBus modBus) {
        CONTAINER = container;
        modBus.addListener(NetworkHandler::registerPayloads);
        INSTANCE = new SleepingOverhaul(new ModPlatformForge());
        NeoForge.EVENT_BUS.register(new LeafLitterSleepEvents());
    }
}
