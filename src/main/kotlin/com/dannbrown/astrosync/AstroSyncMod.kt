package com.dannbrown.astrosync

import com.dannbrown.astrosync.core.FileUtils
import com.dannbrown.astrosync.core.NetworkUtils
import com.dannbrown.astrosync.core.UnzipUtils
import com.dannbrown.astrosync.core.VersionUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(AstroSyncMod.MOD_ID)
object AstroSyncMod {
    const val MOD_ID = "astrosync"
    const val NAME = "Astro Sync"


    @JvmField
    var needsUpdate: Boolean = false
    @JvmField
    var screenShown: Boolean = false
    @JvmField
    var newVersion: String = ""
    @JvmField
    var versionDownloaded: Boolean = false

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    init {
        ModLoadingContext.get()
          .registerConfig(ModConfig.Type.COMMON, AstroSyncConfig.SPEC, "$MOD_ID-common.toml")

        val obj = runForDist(
            clientTarget = {
                MOD_BUS.addListener(::onClientSetup)
                Minecraft.getInstance()
            },
            serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
                "test"
            })

        println(obj)
    }

    /**
     * This is used for initializing client specific
     * things such as renderers and keymaps
     * Fired on the mod specific event bus.
     */
    private fun onClientSetup(event: FMLClientSetupEvent) {
        LOGGER.log(Level.INFO, "Initializing client...")
        checkForUpdates()
    }

    /**
     * Fired on the global Forge bus.
     */
    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        LOGGER.log(Level.INFO, "${NAME} was loaded on the server, doing nothing.")
    }

    private fun checkForUpdates() {
        // if no repo name is set, return
        if (AstroSyncConfig.RepoName.get().isEmpty() || AstroSyncConfig.RepoOwner.get().isEmpty()) {
            LOGGER.log(Level.ERROR, "Repo name or owner not set, cannot check for updates")
            return
        }

        // Check if the version is updated
        val latestRelease = NetworkUtils.fetchLatestRelease()
        if (latestRelease != null) {
            val releaseVersion = VersionUtils.getReleaseVersion(latestRelease)
            val needsUpdate = VersionUtils.isVersionUpdated(AstroSyncConfig.RepoName.get(), releaseVersion)
            this.needsUpdate = needsUpdate

            if(!needsUpdate) {
                LOGGER.log(Level.INFO, "No new version available")
                FileUtils.cleanTempFolder()
                return
            }

            // Version from API is higher than the current version, update
            LOGGER.log(Level.INFO, "New version available: $releaseVersion")
            this.newVersion = releaseVersion

            // Get the download URL for the new version
            val downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, AstroSyncConfig.RepoName.get(), releaseVersion)

            // if download url does not exist, return
            if (downloadUrl == null) {
                LOGGER.log(Level.ERROR, "Download URL not found")
                return
            }

            // Download the new version zip file
            FileUtils.downloadZipFile(downloadUrl)

            // Get folders from new version zip
            val zipPath = FileUtils.TEMP_PATH?.resolve(FileUtils.TEMP_ZIP_NAME)

            // if zipPath does not exist, return
            if (zipPath == null) {
                LOGGER.log(Level.ERROR, "Zip file not found")
                return
            }
            val folders = UnzipUtils.listZipFolders(zipPath.toPath())
            if(folders.isEmpty()) {
                LOGGER.log(Level.ERROR, "Stop: The modpack zip file was empty or corrupted.")
                return
            }

            // Delete old version folders
            FileUtils.deleteOldVersionFolders(folders)

            // Extract new version folders
            if(FileUtils.PATH == null) {
                LOGGER.log(Level.ERROR, "Minecraft path not found")
                return
            }
            LOGGER.log(Level.INFO, "Extracting new version files...")
            UnzipUtils.unzip(zipPath.toPath(), FileUtils.PATH)
            LOGGER.log(Level.INFO, "New version extracted successfully")

            // Update version file
            FileUtils.upsertVersionFile(AstroSyncConfig.RepoName.get(), releaseVersion)
            LOGGER.log(Level.INFO, "Version file updated to $releaseVersion")

            // Clean up temp folder
            FileUtils.cleanTempFolder()
            this.versionDownloaded = true
        }
    }
}