package com.arsskyfix.client;

import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public final class SkyHybridRenderer {

    private SkyHybridRenderer() {
    }

    public static void render(SkyBlockTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        Vec3 cam = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double bx = pos.getX() + 0.5, by = pos.getY() + 0.5, bz = pos.getZ() + 0.5;
        Matrix4f matrix = pose.last().pose();
        VertexConsumer vc = null;
        int occluded = -1; // lazy: only pay for the line-of-sight check once a punch face actually renders

        for (int f = 0; f < CubeFaces.FACES.length; f++) {
            float[] n = CubeFaces.NORMALS[f];
            // only the face pointing at the camera renders, so a back face never punches far-depth
            // across the whole silhouette (which erased the top face and blocks resting on it)
            double dot = (cam.x - (bx + n[0] * 0.5)) * n[0]
                + (cam.y - (by + n[1] * 0.5)) * n[1]
                + (cam.z - (bz + n[2] * 0.5)) * n[2];
            if (dot <= 0) {
                continue;
            }
            if (f == 0) {
                // top face: flat sky tint via the cheap gbuffers_basic fallback (no atmosphere shader).
                // looking down at a floor of sky blocks only ever reaches here, so no punch and no
                // occlusion ray runs, and the expensive skybasic shader is avoided -> cheap for big builds
                SkyFlatRenderer.renderTop(tile, partialTick, pose, buffers);
                continue;
            }
            if (level != null) {
                BlockPos np = pos.relative(CubeFaces.DIRECTIONS[f]);
                var ns = level.getBlockState(np);
                // skip faces against a solid block OR against another sky block (shared inner seams,
                // otherwise a platform of sky blocks shows punched-through stripes between them)
                if (ns.is(tile.getBlockState().getBlock()) || ns.isSolidRender(level, np)) {
                    continue;
                }
            }
            // a punch face wants to render: only now check line of sight (once per block). if terrain
            // blocks the view, fall back to the solid skybasic face so the far-depth punch never x-rays.
            if (occluded == -1) {
                occluded = (level != null && SkyOcclusionCache.isOccluded(tile, level)) ? 1 : 0;
            }
            if (occluded == 1) {
                SkySkybasicRenderer.renderFace(f, pose, buffers);
                continue;
            }
            if (vc == null) {
                vc = buffers.getBuffer(HybridType.SKY_HYBRID);
            }
            float[][] c = CubeFaces.FACES[f];
            for (int v = 0; v < 4; v++) {
                vc.addVertex(matrix, c[v][0], c[v][1], c[v][2]);
            }
        }
    }

    private abstract static class HybridType extends RenderType {

        static final RenderType SKY_HYBRID = create(
            "arsskyfix_sky_hybrid",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionShader))
                .setTextureState(new RenderStateShard.EmptyTextureStateShard(
                    () -> {
                        GbufferPrograms.setOverridePhase(WorldRenderingPhase.SKY);
                        GL11.glDepthRange(1.0, 1.0);
                    },
                    () -> {
                        GbufferPrograms.setOverridePhase(null);
                        GL11.glDepthRange(0.0, 1.0);
                    }))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDepthTestState(GREATER_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private HybridType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                           boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
