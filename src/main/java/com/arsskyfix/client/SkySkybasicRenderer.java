package com.arsskyfix.client;

import com.arsskyfix.ArsSkyConfig;
import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.layer.GbufferPrograms;
import net.irisshaders.iris.pipeline.WorldRenderingPhase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SkySkybasicRenderer {

    // vanilla getSkyColor collapses to black in the End; floor to a dim tone so the block is never a
    // white or black hole in dimensions where skybasic doesn't draw a procedural sky (End/Nether).
    private static final float MIN_R = 0.035f, MIN_G = 0.050f, MIN_B = 0.090f;

    // Pure-green marker tint used in ALL dimensions. The injected skybasic marker detects this green
    // glColor and flags the block for deferred1 (-> sky branch: clouds in the overworld, endSkyColor/
    // netherColor in End/Nether). The green itself never shows: deferred1 overwrites the colour once the
    // block is in the sky branch (overworld GetSky, End/Nether dim sky), and the pack sky never is pure
    // green. For unpatched packs the block is already broken (X-ray / no sky), so the tint is harmless.
    private static final float[] MARKER_RGB = {0.0f, 1.0f, 0.0f};

    private SkySkybasicRenderer() {
    }

    public static void render(SkyBlockTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        Matrix4f matrix = pose.last().pose();
        // faces 1..5 = sides + bottom -> pack sky; face 0 = top -> solid config colour (findability)
        VertexConsumer sky = buffers.getBuffer(SkyShadedType.SKY_SHADED);
        for (int f = 1; f < CubeFaces.FACES.length; f++) {
            emitFace(f, sky, matrix, MARKER_RGB);
        }
        emitTopFace(matrix, buffers);
    }

    // face 0 (top): solid config colour via gbuffers_basic (findable), or sky if the marker is disabled.
    // Shared by the skybasic and gbuffers_block render paths.
    static void emitTopFace(Matrix4f matrix, MultiBufferSource buffers) {
        if (ArsSkyConfig.TOP_MARKER.get()) {
            float[] top = {
                ArsSkyConfig.TOP_R.get() / 255.0f,
                ArsSkyConfig.TOP_G.get() / 255.0f,
                ArsSkyConfig.TOP_B.get() / 255.0f
            };
            emitFace(0, buffers.getBuffer(TopType.TOP), matrix, top);
        } else {
            emitFace(0, buffers.getBuffer(SkyShadedType.SKY_SHADED), matrix, MARKER_RGB);
        }
    }

    public static void renderFace(int face, PoseStack pose, MultiBufferSource buffers) {
        emitFace(face, buffers.getBuffer(SkyShadedType.SKY_SHADED), pose.last().pose(), skyColor(0f));
    }

    private static float[] skyColor(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return new float[]{MIN_R, MIN_G, MIN_B};
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 sky = level.getSkyColor(cam, partialTick);
        return new float[]{
            Math.max((float) sky.x, MIN_R),
            Math.max((float) sky.y, MIN_G),
            Math.max((float) sky.z, MIN_B)
        };
    }

    private static void emitFace(int f, VertexConsumer vc, Matrix4f matrix, float[] col) {
        float[][] c = CubeFaces.FACES[f];
        for (int v = 0; v < 4; v++) {
            vc.vertex(matrix, c[v][0], c[v][1], c[v][2]).color(col[0], col[1], col[2], 1.0f).endVertex();
        }
    }

    private abstract static class SkyShadedType extends RenderType {

        static final RenderType SKY_SHADED = create(
            "arsskyfix_sky_shaded",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTextureState(new RenderStateShard.EmptyTextureStateShard(
                    () -> GbufferPrograms.setOverridePhase(WorldRenderingPhase.SKY),
                    () -> GbufferPrograms.setOverridePhase(null)))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private SkyShadedType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                              boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }

    private abstract static class TopType extends RenderType {

        // no SKY phase override -> gbuffers_basic: a flat, occluding vertex colour (not the sky shader)
        static final RenderType TOP = create(
            "arsskyfix_top",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTransparencyState(NO_TRANSPARENCY)
                .setDepthTestState(LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false));

        private TopType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                        boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
