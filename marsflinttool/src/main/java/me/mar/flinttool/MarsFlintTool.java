package me.mar.flinttool;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MarsFlintTool.MODID)
public class MarsFlintTool {
    public static final String MODID = "marsflinttool";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsFlintTool(IEventBus modBus) {
        ModItems.register(modBus);
        NeoForge.EVENT_BUS.register(new PrimitiveProgressionEvents());
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            PrimitiveProgressionClientEvents.register();
        }
    }
}
