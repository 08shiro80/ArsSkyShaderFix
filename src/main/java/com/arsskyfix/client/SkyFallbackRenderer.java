package com.arsskyfix.client;

import com.hollingsworth.arsnouveau.common.block.tile.SkyBlockTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class SkyFallbackRenderer {

    public static final int UP = 0;

    private static final ResourceLocation WHITE = new ResourceLocation("arsskyfix", "textures/white.png");

    private static final float[][] UVS = { {0, 0}, {0, 1}, {1, 1}, {1, 0} };
    // vanilla getSkyColor collapses to ~black at night; floor to a dim blue so the block never
    // becomes a black hole against a shaderpack's lit night sky
    private static final float MIN_R = 0.035f, MIN_G = 0.050f, MIN_B = 0.090f;
    // subtle top-lit gradient: UP full, DOWN darkest, sides between
    private static final float[] FACE_BRIGHTNESS = {1.00f, 0.55f, 0.80f, 0.80f, 0.80f, 0.80f};

    private SkyFallbackRenderer() {
    }

    public static void render(SkyBlockTile tile, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        float[] base = baseColor(partialTick);
        if (base == null) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        Matrix4f matrix = pose.last().pose();
        for (int f = 0; f < CubeFaces.FACES.length; f++) {
            emitFace(f, vc, matrix, base);
        }
    }

    public static void renderFace(int face, float partialTick, PoseStack pose, MultiBufferSource buffers) {
        float[] base = baseColor(partialTick);
        if (base == null) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(WHITE));
        emitFace(face, vc, pose.last().pose(), base);
    }

    private static float[] baseColor(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return null;
        }
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        Vec3 sky = level.getSkyColor(cam, partialTick);
        return new float[]{
            Math.max((float) sky.x, MIN_R),
            Math.max((float) sky.y, MIN_G),
            Math.max((float) sky.z, MIN_B)
        };
    }

    private static void emitFace(int f, VertexConsumer vc, Matrix4f matrix, float[] base) {
        float b = FACE_BRIGHTNESS[f];
        float r = Mth.clamp(base[0] * b, 0f, 1f);
        float g = Mth.clamp(base[1] * b, 0f, 1f);
        float bl = Mth.clamp(base[2] * b, 0f, 1f);
        float[] n = CubeFaces.NORMALS[f];
        float[][] c = CubeFaces.FACES[f];
        int light = LightTexture.FULL_BRIGHT;
        for (int v = 0; v < 4; v++) {
            vc.vertex(matrix, c[v][0] + n[0] * CubeFaces.EPS, c[v][1] + n[1] * CubeFaces.EPS, c[v][2] + n[2] * CubeFaces.EPS)
                .color(r, g, bl, 1.0f)
                .uv(UVS[v][0], UVS[v][1])
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(n[0], n[1], n[2])
                .endVertex();
        }
    }
}
