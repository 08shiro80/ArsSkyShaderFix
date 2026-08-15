package com.arsskyfix;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ArsSkyConfig {

    public enum RenderMode {
        SOLID,
        SKYBASIC,
        HYBRID,
        SHADER
    }

    public static final RenderMode DEFAULT_MODE = RenderMode.SHADER;
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.EnumValue<RenderMode> MODE;
    public static final ModConfigSpec.BooleanValue TOP_MARKER;
    public static final ModConfigSpec.IntValue TOP_R;
    public static final ModConfigSpec.IntValue TOP_G;
    public static final ModConfigSpec.IntValue TOP_B;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        MODE = builder
            .comment(
                "How the Ars Nouveau Sky Block is drawn while an Iris shaderpack is active.",
                "Without a shaderpack the vanilla effect is always used.",
                "  SHADER   - (default) runtime injection: the sky_block sides/bottom become the",
                "             shaderpack's full sky (gradient + sun/moon/stars + clouds) at real depth,",
                "             top stays ground. Supported packs: Complementary Reimagined/Unbound",
                "             (with or without EuphoriaPatches), Photon, BSL, SEUS Renewed, Sildur's",
                "             Vibrant. Other packs stay a plain block - use HYBRID for those.",
                "  HYBRID   - makeshift fallback for unsupported packs: faces open to the air reveal the",
                "             pack's sky (clouds + sun), solid buildable top. Works almost anywhere but",
                "             is fairly inefficient (per-block render-type flushes) and can show minor X-ray.",
                "  SKYBASIC - solid block shaded by the shaderpack's own sky shader. Real sky colour,",
                "             correct occlusion, but no cloud/sun disc.",
                "  SOLID    - flat, sky-tinted cube. Cheapest, always works, no clouds or sun.")
            .defineEnum("renderMode", DEFAULT_MODE);
        TOP_MARKER = builder
            .comment(
                "Mark the sky_block's top face with a solid colour so placed blocks stay findable.",
                "The sides and bottom always show the sky; only the top uses this colour. Set false for a",
                "fully seamless block (top also becomes sky).")
            .define("topMarker", true);
        TOP_R = builder
            .comment(
                "Top-face marker colour (0-255 per channel). Presets:",
                "  lavender 158/140/209 (default), white 255/255/255, magenta 255/0/255,",
                "  red 220/40/40, cyan 60/200/220, amber 235/170/40.")
            .defineInRange("topColorR", 158, 0, 255);
        TOP_G = builder.defineInRange("topColorG", 140, 0, 255);
        TOP_B = builder.defineInRange("topColorB", 209, 0, 255);
        SPEC = builder.build();
    }

    private ArsSkyConfig() {
    }
}
