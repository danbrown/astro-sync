package com.dannbrown.astrosync.core;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class VersionUtils {

  public static final String PLACEHOLDER_VERSION = "0.0.0";

  // Convert version string to List<Integer>
  public static List<Integer> parseVersion(String version) {
    return Arrays.stream(version.replaceFirst("^v", "").split("\\."))
            .map(s -> {
              try {
                return Integer.parseInt(s);
              } catch (NumberFormatException e) {
                return 0;
              }
            })
            .collect(Collectors.toList());
  }

  public static Comparator<List<Integer>> compareVersions() {
    return (version1, version2) -> {
      if (!version1.get(0).equals(version2.get(0))) {
        return version1.get(0) - version2.get(0); // Major
      } else if (!version1.get(1).equals(version2.get(1))) {
        return version1.get(1) - version2.get(1); // Minor
      } else {
        return version1.get(2) - version2.get(2); // Patch
      }
    };
  }

  public static Comparator<GithubRelease> compareReleases() {
    return (release1, release2) -> {
      List<Integer> version1 = parseVersion(release1.getTagName());
      List<Integer> version2 = parseVersion(release2.getTagName());

      if (!version1.get(0).equals(version2.get(0))) {
        return version1.get(0) - version2.get(0);
      } else if (!version1.get(1).equals(version2.get(1))) {
        return version1.get(1) - version2.get(1);
      } else {
        return version1.get(2) - version2.get(2);
      }
    };
  }

  public static boolean isVersionUpdated(String repoName, String currentVersion) {
    String fileVersion = FileUtils.getVersionFromFile(repoName);
    int higherVersion = compareVersions().compare(parseVersion(currentVersion), parseVersion(fileVersion));
    return higherVersion > 0;
  }

  public static String getReleaseVersion(GithubRelease release) {
    return release.getTagName() != null ? release.getTagName() : PLACEHOLDER_VERSION;
  }
}
