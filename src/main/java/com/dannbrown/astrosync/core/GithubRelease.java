package com.dannbrown.astrosync.core;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GithubRelease {

  private String url;

  @SerializedName("assets_url")
  private String assetsUrl;

  @SerializedName("upload_url")
  private String uploadUrl;

  @SerializedName("html_url")
  private String htmlUrl;

  private long id;

  @SerializedName("node_id")
  private String nodeId;

  @SerializedName("tag_name")
  private String tagName;

  @SerializedName("target_commitish")
  private String targetCommitish;

  private String name;
  private boolean draft;
  private boolean prerelease;

  @SerializedName("created_at")
  private String createdAt;

  @SerializedName("published_at")
  private String publishedAt;

  private List<Asset> assets;

  @SerializedName("tarball_url")
  private String tarballUrl;

  @SerializedName("zipball_url")
  private String zipballUrl;

  private String body;

  // --- Getters ---
  public String getUrl() { return url; }
  public String getAssetsUrl() { return assetsUrl; }
  public String getUploadUrl() { return uploadUrl; }
  public String getHtmlUrl() { return htmlUrl; }
  public long getId() { return id; }
  public String getNodeId() { return nodeId; }
  public String getTagName() { return tagName; }
  public String getTargetCommitish() { return targetCommitish; }
  public String getName() { return name; }
  public boolean isDraft() { return draft; }
  public boolean isPrerelease() { return prerelease; }
  public String getCreatedAt() { return createdAt; }
  public String getPublishedAt() { return publishedAt; }
  public List<Asset> getAssets() { return assets; }
  public String getTarballUrl() { return tarballUrl; }
  public String getZipballUrl() { return zipballUrl; }
  public String getBody() { return body; }

  // --- Nested Asset class ---
  public static class Asset {
    private String url;
    private long id;

    @SerializedName("node_id")
    private String nodeId;

    private String name;
    private String label;

    @SerializedName("content_type")
    private String contentType;

    private String state;
    private long size;

    @SerializedName("download_count")
    private int downloadCount;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("browser_download_url")
    private String browserDownloadUrl;

    // --- Getters ---
    public String getUrl() { return url; }
    public long getId() { return id; }
    public String getNodeId() { return nodeId; }
    public String getName() { return name; }
    public String getLabel() { return label; }
    public String getContentType() { return contentType; }
    public String getState() { return state; }
    public long getSize() { return size; }
    public int getDownloadCount() { return downloadCount; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getBrowserDownloadUrl() { return browserDownloadUrl; }
  }
}
