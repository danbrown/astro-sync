package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSync;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public class FileUtils {
  public static final Path PATH = Minecraft.getInstance().gameDirectory.toPath();
  public static final File MOD_PATH = resolvePath(AstroSync.MOD_ID);
  public static final File TEMP_PATH = MOD_PATH != null ? new File(MOD_PATH, "temp") : null;
  public static final String TEMP_ZIP_NAME = "latestVersion.zip";

  static {
    if (MOD_PATH != null && !MOD_PATH.exists()) {
      MOD_PATH.mkdirs();
    }
    if (TEMP_PATH != null && !TEMP_PATH.exists()) {
      TEMP_PATH.mkdirs();
    }
  }

  public static File resolvePath(String folder) {
    return PATH != null ? PATH.resolve(folder).toFile() : null;
  }

  public static String getVersionFromFile(String repoName) {
    File file = MOD_PATH != null ? new File(MOD_PATH, repoName + ".json") : null;
    if (file != null && file.exists()) {
      try {
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        return jsonObject.get("version").getAsString();
      } catch (IOException e) {
        AstroSync.LOGGER.error("Error reading version file: " + e.getMessage());
      }
    }
    upsertVersionFile(repoName, VersionUtils.PLACEHOLDER_VERSION);
    return VersionUtils.PLACEHOLDER_VERSION;
  }

  public static void upsertVersionFile(String repoName, String version) {
    File file = MOD_PATH != null ? new File(MOD_PATH, repoName + ".json") : null;
    if (file != null) {
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("version", version);
      try (FileWriter writer = new FileWriter(file)) {
        writer.write(jsonObject.toString());
      } catch (IOException e) {
        AstroSync.LOGGER.error("Error writing version file: " + e.getMessage());
      }
    }
  }

  public static void downloadZipFile(String url) {
    File oldFile = TEMP_PATH != null ? new File(TEMP_PATH, TEMP_ZIP_NAME) : null;
    if (oldFile != null && oldFile.exists()) {
      oldFile.delete();
      AstroSync.LOGGER.info("Old release detected, deleting...");
    }

    File newFile = TEMP_PATH != null ? new File(TEMP_PATH, TEMP_ZIP_NAME) : null;
    try {
      if (newFile != null) {
        newFile.createNewFile();
        AstroSync.LOGGER.info("Downloading new pack release, wait a moment...");

        try (BufferedInputStream input = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream output = new FileOutputStream(newFile)) {

          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
          }
        }

        AstroSync.LOGGER.info("New release downloaded successfully");
      }
    } catch (Exception e) {
      AstroSync.LOGGER.error("Error downloading new release: " + e.getMessage());
    }
  }

  public static void deleteOldVersionFolders(List<String> folders) {
    for (String folderName : folders) {
      File folder = PATH != null ? PATH.resolve(folderName).toFile() : null;
      if (folder != null && folder.exists()) {
        deleteRecursively(folder);
      }
    }
  }

  public static void cleanTempFolder() {
    if (TEMP_PATH != null && TEMP_PATH.exists()) {
      deleteRecursively(TEMP_PATH);
    }
    AstroSync.LOGGER.info("Temp folder cleaned up");
  }

  private static void deleteRecursively(File file) {
    if (file.isDirectory()) {
      File[] entries = file.listFiles();
      if (entries != null) {
        for (File entry : entries) {
          deleteRecursively(entry);
        }
      }
    }
    file.delete();
  }
}
