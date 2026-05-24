package me.mar.foodspoilage;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

@Mod(MarsFoodSpoilage.MODID)
public class MarsFoodSpoilage {
    public static final String MODID = "marsfoodspoilage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsFoodSpoilage(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MarsSpoilageConfig.SPEC);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        SpoilageEvents events = new SpoilageEvents();
        modBus.addListener(EventPriority.LOWEST, ModifyDefaultComponentsEvent.class, events::makeFoodUnstackable);
        modBus.addListener(this::buildCreativeTabContents);
        NeoForge.EVENT_BUS.register(events);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event ->
                event.addListener(Identifier.fromNamespaceAndPath(MODID, "spoilage_rules_loader"), SpoilageRulesLoader.INSTANCE));
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(new ItemStack(ModBlocks.DRYING_RACK_ITEM.get()));
        }
    }
}
