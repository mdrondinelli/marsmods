package me.mar.campfires;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MarsCampfires.MODID)
public class MarsCampfires {
    public static final String MODID = "marscampfires";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsCampfires(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MarsCampfiresConfig.SPEC);
        ModAttachments.register(modBus);
        NeoForge.EVENT_BUS.register(new CampfireEvents());
    }
}
