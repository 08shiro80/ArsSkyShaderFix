package com.arsskyfix.mixin;

import com.arsskyfix.patch.ArsPatcher;
import java.nio.file.Path;
import net.irisshaders.iris.shaderpack.IdMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IdMap.class, remap = false)
public class IdMapMixin {

    @Inject(
        method = "readProperties(Ljava/nio/file/Path;Ljava/lang/String;)Ljava/lang/String;",
        at = @At("RETURN"),
        cancellable = true,
        remap = false)
    private static void arsskyfix$patchBlockProperties(Path root, String name,
                                                       CallbackInfoReturnable<String> cir) {
        if (!"block.properties".equals(name)) {
            return;
        }
        String source = cir.getReturnValue();
        String patched = ArsPatcher.patchBlockProperties(source);
        if (patched != source) {
            cir.setReturnValue(patched);
        }
    }
}
