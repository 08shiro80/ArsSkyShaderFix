package com.arsskyfix;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod("arsskyfix")
public class ArsSkyShaderFix {
    public ArsSkyShaderFix() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ArsSkyConfig.SPEC);
    }
}
