package me.mar.balancedhunger;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(MarsBalancedHunger.MODID)
public class MarsBalancedHunger {
    public static final String MODID = "marsbalancedhunger";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsBalancedHunger(IEventBus modBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, BalancedHungerConfig.SPEC);
        NeoForge.EVENT_BUS.register(ExhaustionHandlers.class);
    }
}
