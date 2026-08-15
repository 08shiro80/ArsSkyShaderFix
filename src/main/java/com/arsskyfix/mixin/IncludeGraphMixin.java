package com.arsskyfix.mixin;

import com.arsskyfix.patch.ArsPatcher;
import java.nio.file.Path;
import net.irisshaders.iris.shaderpack.include.IncludeGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = IncludeGraph.class, remap = false)
public class IncludeGraphMixin {

    @Inject(
        method = "readFile(Ljava/nio/file/Path;)Ljava/lang/String;",
        at = @At("RETURN"),
        cancellable = true,
        remap = false)
    private static void arsskyfix$patchSource(Path path, CallbackInfoReturnable<String> cir) {
        String source = cir.getReturnValue();
        if (source == null) {
            return;
        }
        String patched = ArsPatcher.patchShaderSource(path, source);
        if (patched != source) {
            cir.setReturnValue(patched);
        }
    }
}
