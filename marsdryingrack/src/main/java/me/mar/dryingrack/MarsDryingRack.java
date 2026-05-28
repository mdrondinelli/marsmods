package me.mar.dryingrack;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MarsDryingRack.MODID)
public class MarsDryingRack {
    public static final String MODID = "marsdryingrack";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsDryingRack(IEventBus modBus) {
        ModBlocks.register(modBus);
        modBus.addListener(this::buildCreativeTabContents);
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event ->
                event.addListener(Identifier.fromNamespaceAndPath(MODID, "drying_recipes_loader"), DryingRecipesLoader.INSTANCE));
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(new ItemStack(ModBlocks.DRYING_RACK_ITEM.get()));
        }
    }
}
