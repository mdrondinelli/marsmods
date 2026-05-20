package com.marlonrondinelli.stoneage;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;

@Mod(MarsStoneAge.MODID)
public class MarsStoneAge {
    public static final String MODID = "marsstoneage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarsStoneAge() {
        LOGGER.info("Loaded Mar's Stone Age");
    }
}
