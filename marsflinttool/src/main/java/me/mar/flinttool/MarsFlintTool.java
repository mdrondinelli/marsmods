package me.mar.flinttool;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(MarsFlintTool.MODID)
public class MarsFlintTool {
    public static final String MODID = "marsflinttool";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsFlintTool() {
        NeoForge.EVENT_BUS.register(new PrimitiveProgressionEvents());
    }
}
