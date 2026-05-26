package me.mar.wildcrops;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MarsWildCrops.MODID)
public class MarsWildCrops {
    public static final String MODID = "marswildcrops";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsWildCrops(IEventBus modBus) {
        ModBlocks.register(modBus);
        ModFeatures.register(modBus);
    }
}
