package com.arsskyfix.patch;

import java.util.List;

public final class PackPatchSet {
    String name;
    List<String> detect;
    // how the mod draws the sky_block for this pack: "skybasic" (default) routes the faces through the
    // pack's gbuffers_skybasic; "block" routes them through gbuffers_block at real depth with a blockEntityId
    // marker (packs that draw their sky in deferred and reconstruct it there, e.g. Photon).
    String render;
    List<Patch> patches;

    String renderVia() {
        return render == null ? "skybasic" : render;
    }

    boolean matches(String packNameLower) {
        if (detect == null || detect.isEmpty() || packNameLower == null) {
            return false;
        }
        for (String needle : detect) {
            if (!packNameLower.contains(needle)) {
                return false;
            }
        }
        return true;
    }
}
