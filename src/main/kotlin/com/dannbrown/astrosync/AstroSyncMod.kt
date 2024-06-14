package com.dannbrown.astrosync

import com.dannbrown.astrosync.core.FileUtils
import com.dannbrown.astrosync.core.NetworkUtils
import com.dannbrown.astrosync.core.UnzipUtils
import com.dannbrown.astrosync.core.VersionUtils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist
import java.io.File

@Mod(AstroSyncMod.MOD_ID)
object AstroSyncMod {
    const val MOD_ID = "astrosync"
    const val NAME = "Astro Sync"

    const val REPO_OWNER = "danbrown" // TODO: make this configurable
    const val REPO_NAME = "minezada" // TODO: make this configurable

    @JvmField
    var needsUpdate: Boolean = false
    @JvmField
    var screenShown: Boolean = false

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)

    init {
        LOGGER.log(Level.INFO, "Hello world!")

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
        // Check if the version is updated
        val latestRelease = NetworkUtils.fetchLatestRelease()
        if (latestRelease != null) {
            val releaseVersion = VersionUtils.getReleaseVersion(latestRelease)
            val needsUpdate = VersionUtils.isVersionUpdated(REPO_NAME, releaseVersion)
            this.needsUpdate = needsUpdate

            if(!needsUpdate) {
                LOGGER.log(Level.INFO, "No new version available")
                FileUtils.cleanTempFolder()
                return
            }

            // Version from API is higher than the current version, update
            LOGGER.log(Level.INFO, "New version available: $releaseVersion")

            // Get the download URL for the new version
            val downloadUrl = NetworkUtils.getReleaseDownloadUrl(latestRelease, REPO_NAME, releaseVersion)

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
            FileUtils.upsertVersionFile(REPO_NAME, releaseVersion)
            LOGGER.log(Level.INFO, "Version file updated to $releaseVersion")

            // Clean up temp folder
            FileUtils.cleanTempFolder()
        }
    }
}