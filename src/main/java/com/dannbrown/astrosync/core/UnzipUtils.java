package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSync;

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
   * directory. Root {@code manifest.json} is never written to disk.
   */
  public static void applyUpdateFromZip(Path zipFilePath, Path gameDirectory) throws IOException {
    if (!Files.exists(gameDirectory)) {
      Files.createDirectories(gameDirectory);
    }
    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      applyManifestDeletes(gameDirectory, UpdateManifest.readDeleteTargets(zip));
      mergeZipEntriesIntoGameDir(zip, gameDirectory);
    }
  }

  /**
   * Merges the zip into the game directory (no manifest handling). Prefer {@link #applyUpdateFromZip}
   * for release zips that may include a manifest.
   */
  public static void unzipMergeIntoGameDir(Path zipFilePath, Path gameDirectory) throws IOException {
    if (!Files.exists(gameDirectory)) {
      Files.createDirectories(gameDirectory);
    }
    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      mergeZipEntriesIntoGameDir(zip, gameDirectory);
    }
  }

  private static void mergeZipEntriesIntoGameDir(ZipFile zip, Path gameDirectory) throws IOException {
    Path gameNorm = gameDirectory.normalize();
    Enumeration<? extends ZipEntry> entries = zip.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      String name = entry.getName();
      if (UpdateManifest.isRootManifestEntry(name)) {
        continue;
      }
      if (isProhibitedPath(name)) {
        AstroSync.LOGGER.info(name + " skipped (prohibited path).");
        continue;
      }

      Path targetPath = gameDirectory.resolve(name).normalize();
      if (!targetPath.startsWith(gameNorm)) {
        AstroSync.LOGGER.warn("Skipping zip entry outside game dir: " + name);
        continue;
      }

      if (entry.isDirectory()) {
        Files.createDirectories(targetPath);
        continue;
      }

      Files.createDirectories(targetPath.getParent());
      try (BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry))) {
        extractFileReplacing(inputStream, targetPath);
      }
    }
  }

  private static void applyManifestDeletes(Path gameDirectory, List<String> relativePaths) throws IOException {
    for (String rel : relativePaths) {
      if (rel == null || rel.isEmpty()) {
        continue;
      }
      if (isProhibitedPath(rel)) {
        AstroSync.LOGGER.warn("manifest delete skipped (prohibited): {}", rel);
        continue;
      }
      Path target = resolveUnderGameDir(gameDirectory, rel);
      if (target == null) {
        AstroSync.LOGGER.warn("manifest delete skipped (outside game dir): {}", rel);
        continue;
      }
      if (Files.isRegularFile(target)) {
        Files.deleteIfExists(target);
        AstroSync.LOGGER.info("Deleted (manifest): {}", rel);
      } else if (Files.isDirectory(target)) {
        AstroSync.LOGGER.warn("manifest delete skipped (not a file): {}", rel);
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
      AstroSync.LOGGER.error("Invalid modpack zip file, or it was incorrectly downloaded.");
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
