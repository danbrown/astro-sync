package com.dannbrown.astrosync.core

import com.google.gson.annotations.SerializedName



class GithubRelease {
  private val url: String? = null

  @SerializedName("assets_url")
  private val assetsUrl: String? = null

  @SerializedName("upload_url")
  private val uploadUrl: String? = null

  @SerializedName("html_url")
  private val htmlUrl: String? = null
  private val id: Long = 0

  @SerializedName("node_id")
  private val nodeId: String? = null

  @SerializedName("tag_name")
  val tagName: String? = null

  @SerializedName("target_commitish")
  private val targetCommitish: String? = null
  private val name: String? = null
  private val draft = false
  private val prerelease = false

  @SerializedName("created_at")
  private val createdAt: String? = null

  @SerializedName("published_at")
  private val publishedAt: String? = null
  val assets: List<Asset>? = null

  @SerializedName("tarball_url")
  private val tarballUrl: String? = null

  @SerializedName("zipball_url")
  private val zipballUrl: String? = null
  private val body: String? = null


  class Asset {
    private val url: String? = null
    private val id: Long = 0

    @SerializedName("node_id")
    private val nodeId: String? = null
    val name: String? = null
    private val label: String? = null

    @SerializedName("content_type")
    private val contentType: String? = null
    private val state: String? = null
    private val size: Long = 0

    @SerializedName("download_count")
    private val downloadCount = 0

    @SerializedName("created_at")
    private val createdAt: String? = null

    @SerializedName("updated_at")
    private val updatedAt: String? = null

    @SerializedName("browser_download_url")
    val browserDownloadUrl: String? = null
  }
}