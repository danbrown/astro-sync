package com.dannbrown.astrosync;

import com.dannbrown.astrosync.core.FileUtils;
import com.dannbrown.astrosync.core.GithubRelease;
import com.dannbrown.astrosync.core.NetworkUtils;
import com.dannbrown.astrosync.core.UnzipUtils;
import com.dannbrown.astrosync.core.VersionUtils;

import java.io.File;

/**
 * Shared update check + apply for client and dedicated server.
 */
public final class AstroSyncUpdate {

    public enum Result {
        /** Repo owner/name not configured. */
        SKIPPED_CONFIG,
        /** Already on latest version or no release. */
        NO_UPDATE,
        /** Download/validation/apply failed. */
        FAILED,
        /** Update zip was applied successfully. */
        APPLIED
    }

    private AstroSyncUpdate() {
    }

    /**
     * Fetches the latest release, downloads if newer, applies manifest + merge into the game directory.
     */
    public static Result checkAndApply() {
        String repoName = AstroSyncConfig.getRepoName();
        String repoOwner = AstroSyncConfig.getRepoOwner();
        if (repoName.isEmpty() || repoOwner.isEmpty()) {
            AstroSync.LOGGER.error("Repo name or owner not set, cannot check for updates");
            return Result.SKIPPED_CONFIG;
        }

        GithubRelease latestRelease = NetworkUtils.fetchLatestRelease(repoOwner, repoName);
        if (latestRelease == null) {
            AstroSync.LOGGER.warn("No GitHub release found");
            return Result.FAILED;
        }

        String releaseVersion = VersionUtils.getReleaseVersion(latestRelease);
        boolean needsUpdate = VersionUtils.isVersionUpdated(repoName, releaseVersion);

        if (!needsUpdate) {
            AstroSync.LOGGER.info("No new version available");
            FileUtils.cleanTempFolder();
            return Result.NO_UPDATE;
        }

        AstroSync.LOGGER.info("New version available: " + releaseVersion);
        AstroSync.newVersion = releaseVersion;
        AstroSync.needsUpdate = true;

        String downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, repoName, releaseVersion);
        if (downloadUrl == null) {
            AstroSync.LOGGER.error("Download URL not found");
            return Result.FAILED;
        }

        FileUtils.downloadZipFile(downloadUrl);

        File zipPath = FileUtils.TEMP_PATH != null ? new File(FileUtils.TEMP_PATH, FileUtils.TEMP_ZIP_NAME) : null;
        if (zipPath == null || !zipPath.isFile()) {
            AstroSync.LOGGER.error("Zip file not found");
            return Result.FAILED;
        }

        if (FileUtils.PATH == null) {
            AstroSync.LOGGER.error("Game directory not found");
            return Result.FAILED;
        }

        boolean hasContent;
        try {
            hasContent = UnzipUtils.hasUpdatableContent(zipPath.toPath(), FileUtils.PATH);
        } catch (Exception e) {
            AstroSync.LOGGER.error("Error reading zip file: " + e.getMessage());
            return Result.FAILED;
        }

        if (!hasContent) {
            AstroSync.LOGGER.error("Stop: The modpack zip has nothing to apply (no mergeable files and no valid manifest deletes).");
            return Result.FAILED;
        }

        try {
            AstroSync.LOGGER.info("Applying update (manifest deletes, then merge)...");
            UnzipUtils.applyUpdateFromZip(zipPath.toPath(), FileUtils.PATH);
            AstroSync.LOGGER.info("Update applied successfully");
        } catch (Exception e) {
            AstroSync.LOGGER.error("Error applying update: " + e.getMessage(), e);
            return Result.FAILED;
        }

        FileUtils.upsertVersionFile(repoName, releaseVersion);
        AstroSync.LOGGER.info("Version file updated to " + releaseVersion);

        FileUtils.cleanTempFolder();
        AstroSync.versionDownloaded = true;
        return Result.APPLIED;
    }
}
