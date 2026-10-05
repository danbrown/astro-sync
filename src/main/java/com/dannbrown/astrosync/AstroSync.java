package com.dannbrown.astrosync;

import net.neoforged.fml.common.Mod;

@Mod(AstroSync.MOD_ID)
public class AstroSync {
    public static final String MOD_ID = "astrosync";
    public static final String NAME = "Astro Sync";

    public AstroSync() {
        AstroSyncConfig.load();
    }
}
