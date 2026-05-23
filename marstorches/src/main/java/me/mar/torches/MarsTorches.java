package me.mar.torches;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.mar.torches.config.TorchConfig;
import me.mar.torches.registry.TorchRegistry;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(MarsTorches.MODID)
public class MarsTorches {

    public static final String MODID = "marstorches";
    public static final Logger LOGGER = LogManager.getLogger();

    public MarsTorches(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, TorchConfig.SPEC);

        NeoForge.EVENT_BUS.register(new TorchEvents());
        modEventBus.addListener(this::buildCreativeTabContents);

        TorchRegistry.FEATURE_REGISTRY.register(modEventBus);
        TorchRegistry.CONFIGURED_FEATURE_REGISTRY.register(modEventBus);
        TorchRegistry.PLACED_FEATURE_REGISTRY.register(modEventBus);
        TorchRegistry.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        TorchRegistry.ITEM_REGISTRY.register(modEventBus);
        TorchRegistry.BLOCK_REGISTRY.register(modEventBus);
        TorchRegistry.LOOT_CONDITION_REGISTRY.register(modEventBus);
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(new ItemStack(TorchRegistry.MATCHBOX_ITEM.get()));
        } else if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(new ItemStack(TorchRegistry.LIT_TORCH_ITEM.get()));
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(TorchRegistry.GLOWSTONE_CRYSTAL_ITEM.get()));
            event.accept(new ItemStack(TorchRegistry.GLOWSTONE_PASTE_ITEM.get()));
        }
    }
}
