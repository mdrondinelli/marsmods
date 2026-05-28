package me.mar.baking;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

@Mod(MarsBaking.MODID)
public class MarsBaking {
    public static final String MODID = "marsbaking";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsBaking(IEventBus modBus) {
        ModItems.register(modBus);
        modBus.addListener(this::buildCreativeTabContents);
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FOOD_AND_DRINKS) {
            event.accept(new ItemStack(ModItems.DOUGH.get()));
        }
    }
}
