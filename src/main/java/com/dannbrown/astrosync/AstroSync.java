package com.dannbrown.astrosync;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(AstroSync.MOD_ID)
public class AstroSync {
    public static final String MOD_ID = "astrosync";
    public static final String NAME = "Astro Sync";

    public static boolean needsUpdate = false;
    public static boolean screenShown = false;
    public static String newVersion = "";
    public static boolean versionDownloaded = false;

    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public AstroSync(IEventBus modEventBus, ModContainer modContainer) {
        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, AstroSyncConfig.SPEC, MOD_ID + "-common.toml");
    }
}
