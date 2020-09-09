package com.kieronquinn.app.taptap.utils.github

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class GitHubReleaseResponse {
    @SerializedName("url")
    @Expose
    var url: String? = null

    @SerializedName("assets_url")
    @Expose
    var assetsUrl: String? = null

    @SerializedName("upload_url")
    @Expose
    var uploadUrl: String? = null

    @SerializedName("html_url")
    @Expose
    var htmlUrl: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("node_id")
    @Expose
    var nodeId: String? = null

    @SerializedName("tag_name")
    @Expose
    var tagName: String? = null

    @SerializedName("target_commitish")
    @Expose
    var targetCommitish: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("draft")
    @Expose
    var draft: Boolean? = null

    @SerializedName("author")
    @Expose
    var author: Author? = null

    @SerializedName("prerelease")
    @Expose
    var prerelease: Boolean? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("published_at")
    @Expose
    var publishedAt: String? = null

    @SerializedName("assets")
    @Expose
    var assets: List<Asset>? = null

    @SerializedName("tarball_url")
    @Expose
    var tarballUrl: String? = null

    @SerializedName("zipball_url")
    @Expose
    var zipballUrl: String? = null

    @SerializedName("body")
    @Expose
    var body: String? = null
}