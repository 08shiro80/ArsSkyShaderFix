package com.arsskyfix.client;

import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SkyFlatRenderer {

    private static final float MIN_R = 0.035f, MIN_G = 0.050f, MIN_B = 0.090f;

    private SkyFlatRenderer() {
    }

    public static void renderTop(SkyBlockTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Vec3 sky = level.getSkyColor(mc.gameRenderer.getMainCamera().getPosition(), partialTick);
        float r = Math.max((float) sky.x, MIN_R);
        float g = Math.max((float) sky.y, MIN_G);
        float b = Math.max((float) sky.z, MIN_B);

        VertexConsumer vc = buffers.getBuffer(FlatType.FLAT_SKY);
        Matrix4f matrix = pose.last().pose();
        float[][] c = CubeFaces.FACES[0];
        for (int v = 0; v < 4; v++) {
            vc.addVertex(matrix, c[v][0], c[v][1], c[v][2]).setColor(r, g, b, 1.0f);
        }
    }

    private abstract static class FlatType extends RenderType {

        // getPositionColorShader with no SKY phase override -> Iris picks BASIC_COLOR (gbuffers_basic),
        // the cheapest fallback: flat vertex colour, no atmosphere/fog/lighting per fragment.
        static final RenderType FLAT_SKY = create(
            "arsskyfix_flat_sky",
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

        private FlatType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
                         boolean affectsCrumbling, boolean sortOnUpload, Runnable setup, Runnable clear) {
            super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setup, clear);
        }
    }
}
