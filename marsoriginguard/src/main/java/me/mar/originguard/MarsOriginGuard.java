package me.mar.originguard;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(MarsOriginGuard.MODID)
public class MarsOriginGuard {
    public static final String MODID = "marsoriginguard";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsOriginGuard(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, OriginGuardConfig.SPEC);
    }
}
