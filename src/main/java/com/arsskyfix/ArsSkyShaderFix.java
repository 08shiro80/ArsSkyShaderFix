package com.arsskyfix;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("arsskyfix")
public class ArsSkyShaderFix {
    public ArsSkyShaderFix() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ArsSkyConfig.SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.arsskyfix.client.ArsSkyConfigScreen.register();
        }
    }
}
