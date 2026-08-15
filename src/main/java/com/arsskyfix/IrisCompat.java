package com.arsskyfix;

import net.irisshaders.iris.api.v0.IrisApi;
import net.neoforged.fml.ModList;

/**
 * Isolates the only stable Iris surface we rely on ({@code IrisApi#isShaderPackInUse}). The actual
 * IrisApi reference lives in a nested holder so the class is only loaded when Iris is present,
 * keeping the addon safe when Iris is not installed.
 */
public final class IrisCompat {
    private static final boolean LOADED = ModList.get().isLoaded("iris");

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
