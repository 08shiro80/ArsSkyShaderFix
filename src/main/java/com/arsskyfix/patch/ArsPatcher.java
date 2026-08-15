package com.arsskyfix.patch;

import com.arsskyfix.ArsSkyConfig;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Runtime shader patcher (EuphoriaPatcher-style). Detects the loaded Iris shaderpack and injects the
 * per-family sky_block patches into the in-memory shader/property sources, so no on-disk pack edits are
 * needed. Patch data lives in {@code assets/arsskyfix/patches/<family>.json} (byte-exact from the
 * confirmed pre-patched packs).
 */
public final class ArsPatcher {
    private static final Logger LOGGER = LogManager.getLogger("arsskyfix.patch");
    private static final String[] FAMILIES = {"complementary", "photon", "bsl", "seus", "sildurs"};

    private static volatile List<PackPatchSet> registry;
    private static volatile PackPatchSet active;
    private static volatile String activePackName;

    private ArsPatcher() {
    }

    // render path for the currently active pack: "skybasic" (default) or "block" (gbuffers_block + deferred)
    public static String activeRenderVia() {
        ensureDetected();
        PackPatchSet a = active;
        return a == null ? "skybasic" : a.renderVia();
    }

    private static String currentPackName() {
        try {
            return net.irisshaders.iris.Iris.getIrisConfig().getShaderPackName()
                    .map(s -> s.toLowerCase(java.util.Locale.ROOT)).orElse(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void ensureDetected() {
        String name = currentPackName();
        if (name == null) {
            active = null;
            activePackName = null;
            return;
        }
        if (name.equals(activePackName)) {
            return;
        }
        activePackName = name;
        detect(name);
    }

    static void detect(String packNameLower) {
        active = null;
        for (PackPatchSet set : registry()) {
            if (set.matches(packNameLower)) {
                active = set;
                LOGGER.info("Detected shaderpack '{}' -> family '{}', runtime sky_block injection active",
                        packNameLower, set.name);
                return;
            }
        }
        LOGGER.info("Shaderpack '{}' is not a supported family -> no injection", packNameLower);
    }

    public static String patchShaderSource(Path path, String source) {
        if (source == null || !injectionEnabled()) {
            return source;
        }
        ensureDetected();
        PackPatchSet set = active;
        if (set == null) {
            return source;
        }
        String norm = path.toString().replace('\\', '/');
        String result = source;
        for (Patch p : set.patches) {
            if ("block.properties".equals(p.file) || !matchesPath(norm, p.file)) {
                continue;
            }
            result = p.applyShader(result, LOGGER, set.name);
        }
        return result;
    }

    public static String patchBlockProperties(String source) {
        if (!injectionEnabled()) {
            return source;
        }
        ensureDetected();
        PackPatchSet set = active;
        if (set == null) {
            return source;
        }
        String result = source;
        for (Patch p : set.patches) {
            if ("block.properties".equals(p.file)) {
                result = p.applyProperties(result, LOGGER, set.name);
            }
        }
        return result;
    }

    private static boolean matchesPath(String norm, String rel) {
        return norm.equals(rel)
                || norm.equals("shaders/" + rel)
                || norm.endsWith("/shaders/" + rel);
    }

    private static boolean injectionEnabled() {
        try {
            return ArsSkyConfig.MODE.get() == ArsSkyConfig.RenderMode.SHADER;
        } catch (Throwable t) {
            // Iris compiles the pack before our config is loaded at startup; assume the default
            // (SHADER) so that first compile is already injected instead of needing a reload.
            return ArsSkyConfig.DEFAULT_MODE == ArsSkyConfig.RenderMode.SHADER;
        }
    }

    private static List<PackPatchSet> registry() {
        List<PackPatchSet> local = registry;
        if (local != null) {
            return local;
        }
        synchronized (ArsPatcher.class) {
            if (registry != null) {
                return registry;
            }
            Gson gson = new Gson();
            List<PackPatchSet> loaded = new ArrayList<>();
            for (String family : FAMILIES) {
                try (InputStream in = ArsPatcher.class.getResourceAsStream(
                        "/assets/arsskyfix/patches/" + family + ".json")) {
                    if (in == null) {
                        LOGGER.warn("Missing patch resource for family '{}'", family);
                        continue;
                    }
                    PackPatchSet set = gson.fromJson(
                            new InputStreamReader(in, StandardCharsets.UTF_8), PackPatchSet.class);
                    loaded.add(set);
                } catch (Exception e) {
                    LOGGER.error("Failed to load patch set '{}'", family, e);
                }
            }
            registry = loaded;
            return loaded;
        }
    }
}
