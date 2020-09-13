package com.kieronquinn.app.taptap.utils.github

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Asset {
    @SerializedName("url")
    @Expose
    var url: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("node_id")
    @Expose
    var nodeId: String? = null

    @SerializedName("name")
    @Expose
    var name: String? = null

    @SerializedName("label")
    @Expose
    var label: Any? = null

    @SerializedName("uploader")
    @Expose
    var uploader: Uploader? = null

    @SerializedName("content_type")
    @Expose
    var contentType: String? = null

    @SerializedName("state")
    @Expose
    var state: String? = null

    @SerializedName("size")
    @Expose
    var size: Int? = null

    @SerializedName("download_count")
    @Expose
    var downloadCount: Int? = null

    @SerializedName("created_at")
    @Expose
    var createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    var updatedAt: String? = null

    @SerializedName("browser_download_url")
    @Expose
    var browserDownloadUrl: String? = null
}