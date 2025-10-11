package com.dannbrown.astrosync;

import com.dannbrown.astrosync.core.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.io.File;
import java.util.List;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = AstroSync.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = AstroSync.MOD_ID, value = Dist.CLIENT)
public class AstroSyncClient {
    public AstroSyncClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(AstroSync.MOD_ID)) {
            Minecraft.getInstance().execute(AstroSyncClient::checkForUpdates);
        }
    }

    static private void checkForUpdates() {
        String REPNAME = AstroSyncConfig.RepoName.get();
        String REPOOWNER = AstroSyncConfig.RepoOwner.get();
        if (REPNAME.isEmpty() || REPOOWNER.isEmpty()) {
            AstroSync.LOGGER.error("Repo name or owner not set, cannot check for updates");
            return;
        }

        GithubRelease latestRelease = NetworkUtils.fetchLatestRelease(REPOOWNER, REPNAME);
        if (latestRelease != null) {
            String releaseVersion = VersionUtils.getReleaseVersion(latestRelease);
            AstroSync.needsUpdate = VersionUtils.isVersionUpdated(REPNAME, releaseVersion);

            if (!AstroSync.needsUpdate) {
                AstroSync.LOGGER.info("No new version available");
                FileUtils.cleanTempFolder();
                return;
            }

            AstroSync.LOGGER.info("New version available: " + releaseVersion);
            AstroSync.newVersion = releaseVersion;

            String downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, REPNAME, releaseVersion);

            if (downloadUrl == null) {
                AstroSync.LOGGER.error("Download URL not found");
                return;
            }

            FileUtils.downloadZipFile(downloadUrl);

            File zipPath = FileUtils.TEMP_PATH != null ? new File(FileUtils.TEMP_PATH, FileUtils.TEMP_ZIP_NAME) : null;
            if (zipPath == null) {
                AstroSync.LOGGER.error("Zip file not found");
                return;
            }

            List<String> folders;
            try {
                folders = UnzipUtils.listZipFolders(zipPath.toPath());
            } catch (Exception e) {
                AstroSync.LOGGER.error("Error reading zip file: " + e.getMessage());
                return;
            }

            if (folders.isEmpty()) {
                AstroSync.LOGGER.error("Stop: The modpack zip file was empty or corrupted.");
                return;
            }

            FileUtils.deleteOldVersionFolders(folders);

            if (FileUtils.PATH == null) {
                AstroSync.LOGGER.error("Minecraft path not found");
                return;
            }

            try {
                AstroSync.LOGGER.info("Extracting new version files...");
                UnzipUtils.unzip(zipPath.toPath(), FileUtils.PATH);
                AstroSync.LOGGER.info("New version extracted successfully");
            } catch (Exception e) {
                AstroSync.LOGGER.error("Error extracting files: " + e.getMessage());
                return;
            }

            FileUtils.upsertVersionFile(REPNAME, releaseVersion);
            AstroSync.LOGGER.info("Version file updated to " + releaseVersion);

            FileUtils.cleanTempFolder();
            AstroSync.versionDownloaded = true;
        }
    }
}
