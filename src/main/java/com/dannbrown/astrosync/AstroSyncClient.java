package com.dannbrown.astrosync;

import com.dannbrown.astrosync.core.FileUtils;
import com.dannbrown.astrosync.core.GithubRelease;
import com.dannbrown.astrosync.core.NetworkUtils;
import com.dannbrown.astrosync.core.UnzipUtils;
import com.dannbrown.astrosync.core.VersionUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import java.io.File;

public class AstroSyncClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AstroSyncConfig.load();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.execute(AstroSyncClient::checkForUpdates));
    }

    private static void checkForUpdates() {
        String repoName = AstroSyncConfig.getRepoName();
        String repoOwner = AstroSyncConfig.getRepoOwner();
        if (repoName.isEmpty() || repoOwner.isEmpty()) {
            AstroSync.LOGGER.error("Repo name or owner not set, cannot check for updates");
            return;
        }

        GithubRelease latestRelease = NetworkUtils.fetchLatestRelease(repoOwner, repoName);
        if (latestRelease != null) {
            String releaseVersion = VersionUtils.getReleaseVersion(latestRelease);
            AstroSync.needsUpdate = VersionUtils.isVersionUpdated(repoName, releaseVersion);

            if (!AstroSync.needsUpdate) {
                AstroSync.LOGGER.info("No new version available");
                FileUtils.cleanTempFolder();
                return;
            }

            AstroSync.LOGGER.info("New version available: " + releaseVersion);
            AstroSync.newVersion = releaseVersion;

            String downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, repoName, releaseVersion);

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

            if (FileUtils.PATH == null) {
                AstroSync.LOGGER.error("Minecraft path not found");
                return;
            }

            boolean hasContent;
            try {
                hasContent = UnzipUtils.hasUpdatableContent(zipPath.toPath(), FileUtils.PATH);
            } catch (Exception e) {
                AstroSync.LOGGER.error("Error reading zip file: " + e.getMessage());
                return;
            }

            if (!hasContent) {
                AstroSync.LOGGER.error("Stop: The modpack zip has nothing to apply (no mergeable files and no valid manifest deletes).");
                return;
            }

            try {
                AstroSync.LOGGER.info("Applying update (manifest deletes, then merge)...");
                UnzipUtils.applyUpdateFromZip(zipPath.toPath(), FileUtils.PATH);
                AstroSync.LOGGER.info("Update applied successfully");
            } catch (Exception e) {
                AstroSync.LOGGER.error("Error extracting files: " + e.getMessage());
                return;
            }

            FileUtils.upsertVersionFile(repoName, releaseVersion);
            AstroSync.LOGGER.info("Version file updated to " + releaseVersion);

            FileUtils.cleanTempFolder();
            AstroSync.versionDownloaded = true;
        }
    }
}
