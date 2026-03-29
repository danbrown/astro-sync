package com.dannbrown.astrosync;

import com.dannbrown.astrosync.core.FileUtils;
import com.dannbrown.astrosync.core.GithubRelease;
import com.dannbrown.astrosync.core.NetworkUtils;
import com.dannbrown.astrosync.core.UnzipUtils;
import com.dannbrown.astrosync.core.VersionUtils;

import java.io.File;


public final class AstroSyncUpdate {

    private AstroSyncUpdate() {
    }


    public static UpdateApplyResult checkAndApply() {
        String repoName = AstroSyncConfig.getRepoName();
        String repoOwner = AstroSyncConfig.getRepoOwner();
        if (repoName.isEmpty() || repoOwner.isEmpty()) {
            AstroSyncLog.error("Repo name or owner not set, cannot check for updates");
            return UpdateApplyResult.SKIPPED_CONFIG;
        }

        GithubRelease latestRelease = NetworkUtils.fetchLatestRelease(repoOwner, repoName);
        if (latestRelease == null) {
            AstroSyncLog.warn("No GitHub release found");
            return UpdateApplyResult.FAILED;
        }

        String releaseVersion = VersionUtils.getReleaseVersion(latestRelease);
        boolean needsUpdate = VersionUtils.isVersionUpdated(repoName, releaseVersion);

        if (!needsUpdate) {
            AstroSyncLog.info("No new version available");
            FileUtils.cleanTempFolder();
            return UpdateApplyResult.NO_UPDATE;
        }

        AstroSyncLog.info("New version available: " + releaseVersion);

        String downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, repoName, releaseVersion);
        if (downloadUrl == null) {
            AstroSyncLog.error("Download URL not found");
            return UpdateApplyResult.FAILED;
        }

        FileUtils.downloadZipFile(downloadUrl);

        File zipPath = FileUtils.TEMP_PATH != null ? new File(FileUtils.TEMP_PATH, FileUtils.TEMP_ZIP_NAME) : null;
        if (zipPath == null || !zipPath.isFile()) {
            AstroSyncLog.error("Zip file not found");
            return UpdateApplyResult.FAILED;
        }

        if (FileUtils.PATH == null) {
            AstroSyncLog.error("Game directory not found");
            return UpdateApplyResult.FAILED;
        }

        boolean hasContent;
        try {
            hasContent = UnzipUtils.hasUpdatableContent(zipPath.toPath(), FileUtils.PATH);
        } catch (Exception e) {
            AstroSyncLog.error("Error reading zip file: " + e.getMessage());
            return UpdateApplyResult.FAILED;
        }

        if (!hasContent) {
            AstroSyncLog.error("The modpack zip has nothing to apply (no mergeable files and no valid manifest deletes).");
            return UpdateApplyResult.FAILED;
        }

        try {
            AstroSyncLog.info("Applying update (manifest deletes, then merge)...");
            UnzipUtils.applyUpdateFromZip(zipPath.toPath(), FileUtils.PATH, repoName);
            AstroSyncLog.info("Update applied successfully");
        } catch (Exception e) {
            AstroSyncLog.error("Error applying update: " + e.getMessage(), e);
            return UpdateApplyResult.FAILED;
        }

        FileUtils.upsertVersionFile(repoName, releaseVersion);
        AstroSyncLog.info("Version file updated to " + releaseVersion);

        FileUtils.cleanTempFolder();
        return UpdateApplyResult.APPLIED;
    }
}
