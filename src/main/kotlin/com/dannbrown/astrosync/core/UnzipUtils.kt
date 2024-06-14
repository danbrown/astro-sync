package com.dannbrown.astrosync.core

import com.dannbrown.astrosync.AstroSyncMod
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipFile
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

object UnzipUtils {

  val prohibitedFolders = listOf(
    "__MACOSX",
    "saves",
    "options.txt",
    "logs",
    "screenshots",
    "crash-reports",
    "resourcepacks",
    "shaderpacks",
  )

  @Throws(IOException::class)
  fun unzip(zipFilePath: Path, destDirectory: Path) {
    if (!destDirectory.exists()) destDirectory.createDirectories()

    val existingFolders = getExistingFolders(zipFilePath, destDirectory)

    ZipFile(zipFilePath.absolutePathString()).use { zip ->
      zip.entries().asSequence().forEach { entry ->
        if (entry.name.contains(prohibitedFolders.first())) {
          AstroSyncMod.LOGGER.info("${entry.name} is a prohibited folder, skipping extraction.")
          return@forEach
        }

        val filePath = destDirectory.resolve(entry.name)
        if (entry.isDirectory) {
          if (existingFolders.contains(filePath.fileName.toString())) {
            println("${filePath.fileName} already exists. Skipping...")
          } else {
            filePath.createDirectories()
          }
        } else {
          if (!filePath.parent.exists()) {
            filePath.parent?.createDirectories()
          }
          extractFile(BufferedInputStream(zip.getInputStream(entry)), filePath)
        }
      }
    }
  }

  @Throws(IOException::class)
  fun listZipFolders(zipFilePath: Path): List<String> {
    val firstFolders = mutableSetOf<String>()
    ZipFile(zipFilePath.absolutePathString()).use { zip ->
      zip.entries().asSequence().forEach { entry ->
        val name = entry.name
        val firstFolder = name.substringBefore("/")

        if (entry.isDirectory && !firstFolders.contains(firstFolder)) {
          firstFolders.add(firstFolder)
        }
      }
    }
    return firstFolders.toList()
  }

  private fun getExistingFolders(zipFilePath: Path, destDirectory: Path): Set<String> {
    val existingFolders = mutableSetOf<String>()
    ZipFile(zipFilePath.absolutePathString()).use { zip ->
      zip.entries().asSequence().forEach { entry ->
        if (entry.isDirectory) {
          val folderName = destDirectory.resolve(entry.name).fileName.toString()
          if (destDirectory.resolve(folderName).exists()) {
            existingFolders.add(folderName)
          }
        }
      }
    }
    return existingFolders
  }

  @Throws(IOException::class)
  private fun extractFile(inputStream: InputStream, destFilePath: Path) {
    BufferedOutputStream(FileOutputStream(destFilePath.absolutePathString())).use { bos ->
      var read: Int
      val bytesIn = ByteArray(4096)
      while (inputStream.read(bytesIn).also { read = it } != -1) {
        bos.write(bytesIn, 0, read)
      }
    }
  }
}
