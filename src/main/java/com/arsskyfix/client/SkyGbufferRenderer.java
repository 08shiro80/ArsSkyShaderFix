package com.arsskyfix.client;

import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;

// Render path for packs that draw their sky in deferred (e.g. Photon). The sides + bottom go through the
// entity-solid shader, which Oculus routes (during the block-entity render) via ShaderKey.BLOCK_ENTITY to
// gbuffers_block at real depth -> universal occlusion + blockEntityId marker (set by Oculus from
// block.properties). A tiny per-pack gbuffers_block injection turns that blockEntityId into the pack's
// material_mask, and the pack's own deferred sky injection (draw_sky) then paints the sky over these faces.
// The block colour here is irrelevant (deferred overwrites it); the top face uses the shared config marker.
public final class SkyGbufferRenderer {

    private SkyGbufferRenderer() {
    }

    public static void render(SkyBlockTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        PoseStack.Pose last = pose.last();
        Matrix4f matrix = last.pose();
        VertexConsumer vc = buffers.getBuffer(GbufferType.GBUFFER);
        for (int f = 1; f < CubeFaces.FACES.length; f++) {
            float[][] c = CubeFaces.FACES[f];
            float[] n = CubeFaces.NORMALS[f];
            for (int v = 0; v < 4; v++) {
                vc.vertex(matrix, c[v][0], c[v][1], c[v][2])
                    .color(0.0f, 0.0f, 0.0f, 1.0f)
                    .uv(0.0f, 0.0f)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(LightTexture.FULL_BRIGHT)
                    .normal(last.normal(), n[0], n[1], n[2])
                    .endVertex();
            }
        }
        SkySkybasicRenderer.emitTopFace(matrix, buffers);
    }

    private abstract static class GbufferType extends RenderType {

        static final RenderType GBUFFER = create(
            "arsskyfix_gbuffer",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntitySolidShader))
                .setTextureState(BLOCK_SHEET)
                .setTransparencyState(NO_TRANSPARENCY)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private GbufferType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                            boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
