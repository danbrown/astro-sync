package com.dannbrown.astrosync.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

public class NetworkUtils {

  private static List<GithubRelease> fetchReleases(String repoOwner, String repoName) {
    String API_URL = String.format(
            "https://api.github.com/repos/%s/%s/releases",
            repoOwner,
            repoName
    );

    try {
      URL url = new URL(API_URL);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      StringBuilder sb = new StringBuilder();
      String output;
      while ((output = br.readLine()) != null) {
        sb.append(output);
      }
      conn.disconnect();

      String jsonResponse = sb.toString();
      Gson gson = new Gson();
      Type listType = new TypeToken<List<GithubRelease>>() {}.getType();
      return gson.fromJson(jsonResponse, listType);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public static GithubRelease fetchLatestRelease(String repoOwner, String repoName) {
    List<GithubRelease> releases = fetchReleases(repoOwner, repoName);
    if (releases == null || releases.isEmpty()) {
      return null;
    }

    Comparator<GithubRelease> comparator = VersionUtils.compareReleases();
    return releases.stream().max(comparator).orElse(null);
  }

  public static String getReleaseDownloadUrl(GithubRelease release, String repoName, String version) {
    String assetName = repoName + "-" + version + ".zip";
    if (release.getAssets() == null) {
      return null;
    }
    return release.getAssets().stream()
            .filter(asset -> asset.getName() != null && asset.getName().equals(assetName))
            .map(GithubRelease.Asset::getBrowserDownloadUrl)
            .findFirst()
            .orElse(null);
  }
}
