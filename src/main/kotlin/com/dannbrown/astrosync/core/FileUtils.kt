package com.dannbrown.astrosync.core

import com.dannbrown.astrosync.AstroSyncMod
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.Level
import java.io.File
import java.net.URL
import java.nio.file.Path

object FileUtils {
  val PATH: Path? = Minecraft.getInstance().gameDirectory.toPath()
  val MOD_PATH = resolvePath(AstroSyncMod.MOD_ID)
  val TEMP_PATH = MOD_PATH?.toPath()?.resolve("temp")?.toFile()
  val TEMP_ZIP_NAME = "latestVersion.zip"

  init{
    if (MOD_PATH != null && !MOD_PATH.exists()) {
      MOD_PATH.mkdirs()
    }
    if (TEMP_PATH != null && !TEMP_PATH.exists()) {
      TEMP_PATH.mkdirs()
    }
  }

  fun resolvePath(folder: String): File? {
    return if ((PATH != null)) PATH.resolve(folder).toFile() else null;
  }

  fun gerVersionFromFile(repoName: String): String {
    // get a ${repoName}.txt file from the mod folder
    val file = MOD_PATH?.resolve("${repoName}.txt")
    return if (file != null && file.exists()) {
      file.readText()
    } else {
      upsertVersionFile(repoName, VersionUtils.PLACEHOLDER_VERSION)
      VersionUtils.PLACEHOLDER_VERSION
    }
  }

  fun upsertVersionFile(repoName: String, version: String) {
    val file = MOD_PATH?.resolve("${repoName}.txt")
    file?.writeText(version)
  }

  fun downloadZipFile(url: String) {
    val tempName = TEMP_ZIP_NAME

    // delete the old file if it exists
    val oldFile = TEMP_PATH?.resolve(tempName)
    if (oldFile != null && oldFile.exists()) {
      oldFile.delete()
      AstroSyncMod.LOGGER.info("Old release detected, deleting...")
    }

    // create new file
    val newFile = TEMP_PATH?.resolve(tempName)
    newFile?.createNewFile()

    AstroSyncMod.LOGGER.info("Downloading new pack release, wait a moment...")
    // download the file
    newFile?.outputStream()?.use { output ->
      URL(url).openStream().use { input ->
        input.copyTo(output)
      }
    }

    AstroSyncMod.LOGGER.info("New release downloaded successfully")
  }

  fun deleteOldVersionFolders(folders: List<String>) {
    folders.forEach {
      val folder = PATH?.resolve(it)?.toFile()
      if (folder != null && folder.exists()) {
        folder.deleteRecursively()
      }
    }
  }

  fun cleanTempFolder() {
    val tempFolder = TEMP_PATH
    if (tempFolder != null && tempFolder.exists()) {
      tempFolder.deleteRecursively()
    }
    AstroSyncMod.LOGGER.log(Level.INFO, "Temp folder cleaned up")
  }
}