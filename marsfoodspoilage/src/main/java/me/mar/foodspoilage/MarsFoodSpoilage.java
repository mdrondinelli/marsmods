package me.mar.foodspoilage;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;


@Mod(MarsFoodSpoilage.MODID)
public class MarsFoodSpoilage {
    public static final String MODID = "marsfoodspoilage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsFoodSpoilage(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MarsSpoilageConfig.SPEC);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        NeoForge.EVENT_BUS.register(new SpoilageEvents());
        NeoForge.EVENT_BUS.register(new DryingRackSpoilageBridge());
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event ->
                event.addListener(Identifier.fromNamespaceAndPath(MODID, "spoilage_rules_loader"), SpoilageRulesLoader.INSTANCE));
    }
}
