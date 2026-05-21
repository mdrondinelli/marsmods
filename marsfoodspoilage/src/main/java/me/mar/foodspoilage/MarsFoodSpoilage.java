package me.mar.foodspoilage;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@Mod(MarsFoodSpoilage.MODID)
public class MarsFoodSpoilage {
    public static final String MODID = "marsfoodspoilage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsFoodSpoilage(IEventBus modBus) {
        ModDataComponents.register(modBus);
        SpoilageEvents events = new SpoilageEvents();
        modBus.addListener(EventPriority.LOWEST, ModifyDefaultComponentsEvent.class, events::makeFoodUnstackable);
        NeoForge.EVENT_BUS.register(events);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event ->
                event.addListener(Identifier.fromNamespaceAndPath(MODID, "spoilage_rules_loader"), SpoilageRulesLoader.INSTANCE));
    }
}
