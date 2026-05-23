package me.mar.worbloodfx;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MarsWorBloodFx.MODID)
public class MarsWorBloodFx {
    public static final String MODID = "marsworbloodfx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsWorBloodFx(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MarsWorBloodFxConfig.SPEC);
        NeoForge.EVENT_BUS.register(new BloodFxEvents());
    }
}
