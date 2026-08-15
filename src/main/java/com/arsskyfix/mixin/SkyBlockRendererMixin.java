package com.arsskyfix.mixin;

import com.arsskyfix.ArsSkyConfig;
import com.arsskyfix.IrisCompat;
import com.arsskyfix.client.SkyFallbackRenderer;
import com.arsskyfix.client.SkyGbufferRenderer;
import com.arsskyfix.client.SkyHybridRenderer;
import com.arsskyfix.client.SkySkybasicRenderer;
import com.arsskyfix.patch.ArsPatcher;
import com.hollingsworth.arsnouveau.client.renderer.tile.SkyBlockRenderer;
import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkyBlockRenderer.class, remap = false)
public class SkyBlockRendererMixin {

    @Inject(
        method = "render(Lcom/hollingsworth/arsnouveau/common/block/tile/SkyBlockTile;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void arsskyfix$shaderFallback(SkyBlockTile tile, float partialTick, PoseStack pose,
                                          MultiBufferSource buffers, int light, int overlay, CallbackInfo ci) {
        if (tile.showFacade() || !IrisCompat.isShaderPackInUse()) {
            return;
        }
        // draw nothing into the shadow map so the sky block casts no shadow of its own
        if (IrisCompat.isRenderingShadowPass()) {
            ci.cancel();
            return;
        }
        switch (ArsSkyConfig.MODE.get()) {
            case SHADER -> {
                if ("block".equals(ArsPatcher.activeRenderVia())) {
                    SkyGbufferRenderer.render(tile, partialTick, pose, buffers);
                } else {
                    SkySkybasicRenderer.render(tile, partialTick, pose, buffers);
                }
            }
            case HYBRID -> SkyHybridRenderer.render(tile, partialTick, pose, buffers);
            case SKYBASIC -> SkySkybasicRenderer.render(tile, partialTick, pose, buffers);
            default -> SkyFallbackRenderer.render(tile, partialTick, pose, buffers);
        }
        ci.cancel();
    }
}
