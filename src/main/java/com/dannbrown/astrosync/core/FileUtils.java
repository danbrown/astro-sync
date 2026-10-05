package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSync;
import com.dannbrown.astrosync.AstroSyncLog;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

public class FileUtils {
  public static final Path PATH = FMLPaths.GAMEDIR.get();
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

  /**
   * Appends one line to {@code astrosync/{repoName}.log} (under the game directory).
   */
  public static void appendUpdateLog(String repoName, String line) {
    if (MOD_PATH == null || repoName == null || repoName.isEmpty()) {
      return;
    }
    File logFile = new File(MOD_PATH, repoName + ".log");
    String entry = Instant.now() + " [AstroSync] " + line + System.lineSeparator();
    try {
      Files.writeString(logFile.toPath(), entry, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      AstroSyncLog.error("Failed to write update log " + logFile, e);
    }
  }

  public static String getVersionFromFile(String repoName) {
    File file = MOD_PATH != null ? new File(MOD_PATH, repoName + ".json") : null;
    if (file != null && file.exists()) {
      try {
        String content = new String(Files.readAllBytes(file.toPath()));
        JsonObject jsonObject = JsonParser.parseString(content).getAsJsonObject();
        return jsonObject.get("version").getAsString();
      } catch (IOException e) {
        AstroSyncLog.error("Error reading version file: " + e.getMessage());
      }
    }
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
        AstroSyncLog.error("Error writing version file: " + e.getMessage());
      }
    }
  }

  public static void downloadZipFile(String url) {
    File oldFile = TEMP_PATH != null ? new File(TEMP_PATH, TEMP_ZIP_NAME) : null;
    if (oldFile != null && oldFile.exists()) {
      oldFile.delete();
      AstroSyncLog.info("Old release detected, deleting...");
    }

    File newFile = TEMP_PATH != null ? new File(TEMP_PATH, TEMP_ZIP_NAME) : null;
    try {
      if (newFile != null) {
        newFile.createNewFile();
        AstroSyncLog.info("Downloading new pack release, wait a moment...");

        try (BufferedInputStream input = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream output = new FileOutputStream(newFile)) {

          byte[] buffer = new byte[4096];
          int bytesRead;
          while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
          }
        }

        AstroSyncLog.info("New release downloaded successfully");
      }
    } catch (Exception e) {
      AstroSyncLog.error("Error downloading new release: " + e.getMessage());
    }
  }

  public static void cleanTempFolder() {
    if (TEMP_PATH != null && TEMP_PATH.exists()) {
      deleteRecursively(TEMP_PATH);
    }
    AstroSyncLog.info("Temp folder cleaned up");
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
