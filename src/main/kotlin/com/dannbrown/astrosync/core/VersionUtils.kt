package com.dannbrown.astrosync.core

object VersionUtils {

  const val PLACEHOLDER_VERSION = "0.0.0"

  // Function to convert version string to List<Int>
  fun parseVersion(version: String): List<Int> {
    return version.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
  }

  fun compareVersions(): Comparator<List<Int>> {
    return Comparator { version1, version2 ->
      when {
        version1[0] != version2[0] -> version1[0] - version2[0]  // Compare major version
        version1[1] != version2[1] -> version1[1] - version2[1]  // Compare minor version
        else -> version1[2] - version2[2]  // Compare patch version
      }
    }
  }

  fun compareReleases(): Comparator<GithubRelease> {
    return Comparator { release1, release2 ->
      val version1 = parseVersion(release1.tagName!!)
      val version2 = parseVersion(release2.tagName!!)

      when {
        version1[0] != version2[0] -> version1[0] - version2[0]  // Compare major version
        version1[1] != version2[1] -> version1[1] - version2[1]  // Compare minor version
        else -> version1[2] - version2[2]  // Compare patch version
      }
    }
  }

  fun isVersionUpdated(repoName: String, currentVersion: String): Boolean {
    val fileVersion = FileUtils.getVersionFromFile(repoName)
    val higherVersion = compareVersions().compare(parseVersion(currentVersion), parseVersion(fileVersion))
    return higherVersion > 0
  }

  fun getReleaseVersion(release: GithubRelease): String {
    return release.tagName ?: PLACEHOLDER_VERSION
  }

}