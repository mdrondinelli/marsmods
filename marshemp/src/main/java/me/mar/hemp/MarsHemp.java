package me.mar.hemp;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(MarsHemp.MODID)
public class MarsHemp {
    public static final String MODID = "marshemp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsHemp(IEventBus modBus) {
        ModItems.register(modBus);
        modBus.addListener(this::buildCreativeTabContents);
        NeoForge.EVENT_BUS.register(new HempEvents());
    }

    private void buildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(ModItems.PLANT_FIBER.get()));
        }
    }
}
