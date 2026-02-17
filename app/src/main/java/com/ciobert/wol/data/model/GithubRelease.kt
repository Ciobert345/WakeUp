package com.ciobert.wol.data.model

import com.google.gson.annotations.SerializedName

data class GithubRelease(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("body") val body: String,
    @SerializedName("assets") val assets: List<GithubAsset>
)

data class GithubAsset(
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("name") val name: String,
    @SerializedName("size") val size: Long
)
