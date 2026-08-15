package com.arsskyfix.patch;

import org.apache.logging.log4j.Logger;

public final class Patch {
    String file;
    String op;
    String anchor;
    String payload;
    String absent;

    String applyShader(String src, Logger log, String family) {
        if (absent != null && src != null && src.contains(absent)) {
            return src;
        }
        return switch (op) {
            case "INSERT_AFTER" -> insertAfter(src, log, family);
            case "REPLACE" -> replace(src, log, family);
            default -> {
                log.warn("[{}] unsupported shader op {} for {}", family, op, file);
                yield src;
            }
        };
    }

    String applyProperties(String src, Logger log, String family) {
        return switch (op) {
            case "NEW_FILE" -> (src == null || src.isBlank()) ? payload : src;
            case "APPEND" -> {
                if (src == null) src = "";
                if (src.contains(payload.strip())) yield src;
                yield src.isEmpty() || src.endsWith("\n") ? src + payload : src + "\r\n" + payload;
            }
            case "REMOVE_FIRST" -> {
                if (src == null) yield null;
                int i = src.indexOf(anchor);
                yield i < 0 ? src : src.substring(0, i) + src.substring(i + anchor.length());
            }
            case "REMOVE_ALL" -> src == null ? null : src.replace(anchor, "");
            case "INSERT_AFTER" -> src == null ? null : insertAfter(src, log, family);
            case "REPLACE" -> src == null ? null : replace(src, log, family);
            default -> {
                log.warn("[{}] unsupported properties op {}", family, op);
                yield src;
            }
        };
    }

    private String insertAfter(String src, Logger log, String family) {
        if (src.contains(payload)) {
            return src;
        }
        int i = src.indexOf(anchor);
        if (i < 0) {
            log.warn("[{}] INSERT_AFTER anchor not found in {}", family, file);
            return src;
        }
        int at = i + anchor.length();
        return src.substring(0, at) + payload + src.substring(at);
    }

    private String replace(String src, Logger log, String family) {
        int i = src.indexOf(anchor);
        if (i < 0) {
            if (!src.contains(payload)) {
                log.warn("[{}] REPLACE anchor not found in {}", family, file);
            }
            return src;
        }
        return src.substring(0, i) + payload + src.substring(i + anchor.length());
    }
}
