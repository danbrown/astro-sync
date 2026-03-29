package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSyncLog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnzipUtils {

  private static final List<String> PROHIBITED_FOLDERS = java.util.Arrays.asList(
          "__MACOSX",
          "saves",
          "options.txt",
          "logs",
          "screenshots",
          "crash-reports",
          "resourcepacks",
          "shaderpacks"
  );

  private static boolean isProhibitedPath(String entryName) {
    for (String prohibited : PROHIBITED_FOLDERS) {
      if (entryName.contains(prohibited)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Applies optional root {@code manifest.json} deletes, then merges the rest of the zip into the game
   * directory. Root {@code manifest.json} is never written to disk. Logs each added/replaced/deleted file
   * to {@code astrosync/{repoName}.log}.
   */
  public static void applyUpdateFromZip(Path zipFilePath, Path gameDirectory, String repoName) throws IOException {
    if (!Files.exists(gameDirectory)) {
      Files.createDirectories(gameDirectory);
    }
    FileUtils.appendUpdateLog(repoName, "--- update session start ---");
    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      applyManifestDeletes(gameDirectory, UpdateManifest.readDeleteTargets(zip), repoName);
      mergeZipEntriesIntoGameDir(zip, gameDirectory, repoName);
    }
    FileUtils.appendUpdateLog(repoName, "--- update session end ---");
  }

  /**
   * Merges the zip into the game directory (no manifest handling).
   */
  public static void unzipMergeIntoGameDir(Path zipFilePath, Path gameDirectory) throws IOException {
    if (!Files.exists(gameDirectory)) {
      Files.createDirectories(gameDirectory);
    }
    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      mergeZipEntriesIntoGameDir(zip, gameDirectory, null);
    }
  }

  private static void mergeZipEntriesIntoGameDir(ZipFile zip, Path gameDirectory, String repoName) throws IOException {
    Path gameNorm = gameDirectory.normalize();
    Enumeration<? extends ZipEntry> entries = zip.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      String name = entry.getName().replace('\\', '/');
      if (UpdateManifest.isRootManifestEntry(name)) {
        continue;
      }
      if (isProhibitedPath(name)) {
        AstroSyncLog.info(name + " skipped (prohibited path).");
        continue;
      }

      Path targetPath = gameDirectory.resolve(name).normalize();
      if (!targetPath.startsWith(gameNorm)) {
        AstroSyncLog.warn("Skipping zip entry outside game dir: " + name);
        continue;
      }

      if (entry.isDirectory()) {
        Files.createDirectories(targetPath);
        continue;
      }

      if (SelfModPaths.isThisModFile(targetPath)) {
        AstroSyncLog.warn(
                "Skipping overwrite of this mod's own JAR (remove AstroSync from the pack zip or install the new jar manually; overwriting it while running corrupts the game): "
                        + name);
        if (repoName != null) {
          FileUtils.appendUpdateLog(repoName, "SKIPPED self-mod " + name);
        }
        continue;
      }

      Files.createDirectories(targetPath.getParent());
      boolean existed = Files.isRegularFile(targetPath);
      try (BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry))) {
        extractFileReplacing(inputStream, targetPath);
      }
      if (repoName != null) {
        FileUtils.appendUpdateLog(repoName, (existed ? "REPLACED " : "ADDED ") + name);
      }
    }
  }

  private static void applyManifestDeletes(Path gameDirectory, List<String> relativePaths, String repoName) throws IOException {
    for (String rel : relativePaths) {
      if (rel == null || rel.isEmpty()) {
        continue;
      }
      String norm = rel.replace('\\', '/');
      if (isProhibitedPath(norm)) {
        AstroSyncLog.warn("manifest delete skipped (prohibited): {}", rel);
        continue;
      }
      Path target = resolveUnderGameDir(gameDirectory, norm);
      if (target == null) {
        AstroSyncLog.warn("manifest delete skipped (outside game dir): {}", rel);
        continue;
      }
      if (SelfModPaths.isThisModFile(target)) {
        AstroSyncLog.warn("manifest delete skipped (would remove this mod's own JAR): {}", norm);
        if (repoName != null) {
          FileUtils.appendUpdateLog(repoName, "SKIPPED self-mod-delete " + norm);
        }
        continue;
      }
      if (Files.isRegularFile(target)) {
        Files.deleteIfExists(target);
        AstroSyncLog.info("Deleted (manifest): {}", norm);
        if (repoName != null) {
          FileUtils.appendUpdateLog(repoName, "DELETED " + norm);
        }
      } else if (Files.isDirectory(target)) {
        AstroSyncLog.warn("manifest delete skipped (not a file): {}", rel);
      }
    }
  }

  private static Path resolveUnderGameDir(Path gameDirectory, String relativePath) {
    Path resolved = gameDirectory.resolve(relativePath.replace('\\', '/')).normalize();
    if (!resolved.startsWith(gameDirectory.normalize())) {
      return null;
    }
    return resolved;
  }

  public static boolean isValidZipFile(Path filePath) {
    try (ZipFile ignored = new ZipFile(filePath.toFile())) {
      return true;
    } catch (ZipException e) {
      System.out.println("Invalid ZIP file: " + e.getMessage());
      return false;
    } catch (IOException e) {
      System.out.println("I/O error: " + e.getMessage());
      return false;
    }
  }

  /**
   * True if the zip has at least one file to merge (excluding root {@code manifest.json} and prohibited
   * paths) or at least one applicable {@code manifest.json} delete action.
   */
  public static boolean hasUpdatableContent(Path zipFilePath, Path gameDirectory) throws IOException {
    if (!isValidZipFile(zipFilePath)) {
      AstroSyncLog.error("Invalid modpack zip file, or it was incorrectly downloaded.");
      return false;
    }
    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      if (hasMergeableFileEntries(zip)) {
        return true;
      }
      return hasApplicableManifestDeletes(zip, gameDirectory);
    }
  }

  private static boolean hasMergeableFileEntries(ZipFile zip) {
    Enumeration<? extends ZipEntry> entries = zip.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        continue;
      }
      String name = entry.getName();
      if (isProhibitedPath(name)) {
        continue;
      }
      if (UpdateManifest.isRootManifestEntry(name)) {
        continue;
      }
      return true;
    }
    return false;
  }

  private static boolean hasApplicableManifestDeletes(ZipFile zip, Path gameDirectory) {
    List<String> targets = UpdateManifest.readDeleteTargets(zip);
    Path gameNorm = gameDirectory.normalize();
    for (String rel : targets) {
      if (rel == null || rel.isEmpty()) {
        continue;
      }
      if (isProhibitedPath(rel)) {
        continue;
      }
      Path resolved = gameDirectory.resolve(rel.replace('\\', '/')).normalize();
      if (resolved.startsWith(gameNorm)) {
        return true;
      }
    }
    return false;
  }

  private static void extractFileReplacing(InputStream inputStream, Path destFilePath) throws IOException {
    Path parent = destFilePath.getParent();
    if (parent != null) {
      Files.createDirectories(parent);
    }
    try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(destFilePath))) {
      byte[] buffer = new byte[8192];
      int read;
      while ((read = inputStream.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
    }
  }
}
