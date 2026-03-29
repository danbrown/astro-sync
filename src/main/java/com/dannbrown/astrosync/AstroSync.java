package com.dannbrown.astrosync;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AstroSync implements ModInitializer {
    public static final String MOD_ID = "astrosync";
    public static final String NAME = "Astro Sync";

    public static boolean needsUpdate = false;
    public static boolean screenShown = false;
    public static String newVersion = "";
    public static boolean versionDownloaded = false;

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
    }
}
