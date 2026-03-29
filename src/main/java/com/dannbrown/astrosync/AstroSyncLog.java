package com.dannbrown.astrosync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AstroSyncLog {

    private static final Logger LOG = LoggerFactory.getLogger("AstroSync");

    private AstroSyncLog() {
    }

    public static void info(String message) {
        LOG.info("[AstroSync] {}", message);
    }

    public static void warn(String message) {
        LOG.warn("[AstroSync] {}", message);
    }

    public static void error(String message) {
        LOG.error("[AstroSync] {}", message);
    }

    public static void error(String message, Throwable throwable) {
        LOG.error("[AstroSync] " + message, throwable);
    }

    public static void warn(String template, Object arg) {
        LOG.warn("[AstroSync] " + template, arg);
    }

    public static void info(String template, Object arg) {
        LOG.info("[AstroSync] " + template, arg);
    }
}
