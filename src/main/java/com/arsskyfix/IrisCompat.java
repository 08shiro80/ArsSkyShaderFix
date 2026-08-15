package com.arsskyfix;

import net.irisshaders.iris.api.v0.IrisApi;
import net.minecraftforge.fml.ModList;

/**
 * Isolates the only stable Oculus/Iris surface we rely on ({@code IrisApi#isShaderPackInUse}). The
 * actual IrisApi reference lives in a nested holder so the class is only loaded when Oculus is present,
 * keeping the addon safe when Oculus is not installed.
 */
public final class IrisCompat {
    private static final boolean LOADED = ModList.get().isLoaded("oculus");

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        return LOADED && Holder.inUse();
    }

    public static boolean isRenderingShadowPass() {
        return LOADED && Holder.shadowPass();
    }

    private static final class Holder {
        static boolean inUse() {
            return IrisApi.getInstance().isShaderPackInUse();
        }

        static boolean shadowPass() {
            return IrisApi.getInstance().isRenderingShadowPass();
        }
    }
}
