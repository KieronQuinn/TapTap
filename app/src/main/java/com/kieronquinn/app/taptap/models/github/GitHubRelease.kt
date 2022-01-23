package com.kieronquinn.app.taptap.models.github

import com.google.gson.annotations.SerializedName

class GitHubRelease {

    @SerializedName("html_url")
    val gitHubUrl: String? = null

    @SerializedName("tag_name")
    val tag: String? = null

    @SerializedName("name")
    val versionName: String? = null

    @SerializedName("body")
    val body: String? = null

    @SerializedName("assets")
    val assets: Array<Asset>? = null

    class Asset {
        @SerializedName("browser_download_url")
        val downloadUrl: String? = null

        @SerializedName("content_type")
        val contentType: String? = null

        @SerializedName("name")
        val fileName: String? = null
    }

}