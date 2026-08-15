package com.arsskyfix;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod("arsskyfix")
public class ArsSkyShaderFix {
    public ArsSkyShaderFix(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ArsSkyConfig.SPEC);
    }
}
