package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSync;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public class UnzipUtils {

  private static final List<String> PROHIBITED_FOLDERS = Arrays.asList(
          "__MACOSX",
          "saves",
          "options.txt",
          "logs",
          "screenshots",
          "crash-reports",
          "resourcepacks",
          "shaderpacks"
  );


  public static void unzip(Path zipFilePath, Path destDirectory) throws IOException {
    if (!Files.exists(destDirectory)) {
      Files.createDirectories(destDirectory);
    }

    Set<String> existingFolders = getExistingFolders(zipFilePath, destDirectory);

    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();

        for (String prohibited : PROHIBITED_FOLDERS) {
          if (entry.getName().contains(prohibited)) {
            AstroSync.LOGGER.info(entry.getName() + " is a prohibited folder, skipping extraction.");
            continue;
          }
        }

        Path filePath = destDirectory.resolve(entry.getName());

        if (entry.isDirectory()) {
          if (existingFolders.contains(filePath.getFileName().toString())) {
            System.out.println(filePath.getFileName() + " already exists. Skipping...");
          } else {
            Files.createDirectories(filePath);
          }
        } else {
          if (!Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
          }
          try (BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry))) {
            extractFile(inputStream, filePath);
          }
        }
      }
    }
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

  public static List<String> listZipFolders(Path zipFilePath) throws IOException {
    if (!isValidZipFile(zipFilePath)) {
      AstroSync.LOGGER.error("Invalid modpack zip file, or it was incorrectly downloaded.");
      return Collections.emptyList();
    }

    Set<String> firstFolders = new HashSet<>();

    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        String firstFolder = name.contains("/") ? name.substring(0, name.indexOf("/")) : name;

        if (entry.isDirectory() && !firstFolders.contains(firstFolder)) {
          firstFolders.add(firstFolder);
        }
      }
    }
    return new ArrayList<>(firstFolders);
  }

  private static Set<String> getExistingFolders(Path zipFilePath, Path destDirectory) throws IOException {
    Set<String> existingFolders = new HashSet<>();

    try (ZipFile zip = new ZipFile(zipFilePath.toAbsolutePath().toString())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          String folderName = destDirectory.resolve(entry.getName()).getFileName().toString();
          if (Files.exists(destDirectory.resolve(folderName))) {
            existingFolders.add(folderName);
          }
        }
      }
    }
    return existingFolders;
  }

  private static void extractFile(InputStream inputStream, Path destFilePath) throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFilePath.toAbsolutePath().toString()))) {
      byte[] bytesIn = new byte[4096];
      int read;
      while ((read = inputStream.read(bytesIn)) != -1) {
        bos.write(bytesIn, 0, read);
      }
    }
  }
}
