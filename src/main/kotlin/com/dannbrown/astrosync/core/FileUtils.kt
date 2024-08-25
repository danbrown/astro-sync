package com.dannbrown.astrosync.core

import com.dannbrown.astrosync.AstroSyncMod
import net.minecraft.client.Minecraft
import org.apache.logging.log4j.Level
import java.io.File
import java.net.URL
import java.nio.file.Path
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException

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

  fun getVersionFromFile(repoName: String): String {
    // get a ${repoName}.json file from the mod folder
    val file = MOD_PATH?.resolve("${repoName}.json")
    return if (file != null && file.exists()) {
      // Read and parse the JSON file
      val content = file.readText()
      val jsonObject = JsonParser.parseString(content).asJsonObject
      jsonObject.get("version").asString
    } else {
      upsertVersionFile(repoName, VersionUtils.PLACEHOLDER_VERSION)
      VersionUtils.PLACEHOLDER_VERSION
    }
  }

  fun upsertVersionFile(repoName: String, version: String) {
    val file = MOD_PATH?.resolve("${repoName}.json")

    if (file != null) {
      // Create a JSON object with the version field
      val jsonObject = JsonObject().apply {
        addProperty("version", version)
      }

      // Write the JSON object to the file
      file.writeText(jsonObject.toString())
    }
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
    try {
      newFile?.outputStream()?.use { output ->
        URL(url).openStream().use { input ->
          val buffer = ByteArray(4096)
          var bytesRead: Int
          while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
          }
        }
      }

      AstroSyncMod.LOGGER.info("New release downloaded successfully")
    } catch (e: Exception) {
      AstroSyncMod.LOGGER.error("Error downloading new release: ${e.message}")
    }
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