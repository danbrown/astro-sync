package com.dannbrown.astrosync.core

import com.dannbrown.astrosync.AstroSyncConfig
import com.dannbrown.astrosync.AstroSyncMod
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object NetworkUtils {


  private fun fetchReleases(): List<GithubRelease>? {
    val API_URL = "https://api.github.com/repos/${AstroSyncConfig.RepoOwner.get()}/${AstroSyncConfig.RepoName.get()}/releases"
    try {
      val url = URL(API_URL)
      val conn = url.openConnection() as HttpURLConnection
      conn.requestMethod = "GET"
      conn.setRequestProperty("Accept", "application/json")

      if (conn.responseCode != 200) {
        throw RuntimeException("Failed : HTTP error code : " + conn.responseCode)
      }
      val br = BufferedReader(InputStreamReader((conn.inputStream)))
      val sb = StringBuilder()
      var output: String?
      while ((br.readLine()
          .also { output = it }) != null) {
        sb.append(output)
      }
      conn.disconnect()
      val jsonResponse = sb.toString()
      val gson = Gson()
      val listType = object : TypeToken<List<GithubRelease>>() {}.type
      return gson.fromJson(jsonResponse, listType)
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return null
  }

  fun fetchLatestRelease(): GithubRelease? {
    val releases = fetchReleases()
    // compare all the tagName of each release and return the latest one
    // tagName is "v1.0.0" format
    releases ?: return null

    // Find the release with the highest version number
    var latestRelease: GithubRelease? = null

    // Find the release with the highest version number
    latestRelease = releases.maxWith(VersionUtils.compareReleases())

    return latestRelease
  }

  fun getReleaseDownloadUrl(release: GithubRelease, repoName: String, version: String): String? {
    val assetName = "${repoName}-${version}.zip"
    release.assets ?: return null
    val currentAsset = release.assets.find { it.name == assetName }
    return currentAsset?.browserDownloadUrl
  }




}