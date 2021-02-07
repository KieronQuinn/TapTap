package com.kieronquinn.app.taptap.utils

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Parcelable
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.R
import com.kieronquinn.app.taptap.utils.github.GitHubReleaseResponse
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.File

class UpdateChecker {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    companion object {
        private const val BASE_URL = "https://api.github.com/repos/KieronQuinn/TapTap/"
    }

    var updateAvailable = MutableStateFlow(false)
    var hasDismissedDialog = false

    fun getLatestRelease() = callbackFlow {
        updateAvailable.value = false
        withContext(Dispatchers.IO){
            getReleaseList()?.let { gitHubReleaseResponse ->
                val currentTag = gitHubReleaseResponse.tagName
                if(currentTag != BuildConfig.TAG_NAME){
                    //New update available!
                    val asset = gitHubReleaseResponse.assets!!.firstOrNull { it.name!!.endsWith(".apk") }
                    val releaseUrl = asset?.browserDownloadUrl?.replace("/download/", "/tag/")?.apply {
                        substring(0, lastIndexOf("/"))
                    }
                    offer(Update(gitHubReleaseResponse.name!!, gitHubReleaseResponse.body!!, gitHubReleaseResponse.publishedAt!!, asset?.browserDownloadUrl ?: "https://github.com/KieronQuinn/TapTap/releases", asset?.name ?: "TapTap.apk", releaseUrl ?: "https://github.com/KieronQuinn/TapTap/releases"))
                    updateAvailable.value = true
                }
            } ?: run {
                offer(null)
            }
        }
        awaitClose {  }
    }

    fun clearCachedDownloads(context: Context){
        File(context.externalCacheDir, "updates").deleteRecursively()
    }

    private fun getReleaseList(): GitHubReleaseResponse? {
        val service: GitHubService = retrofit.create(GitHubService::class.java)
        return service.getReleaseList().execute().body()
    }

    interface GitHubService {
        @GET("releases/latest")
        fun getReleaseList(): Call<GitHubReleaseResponse>
    }

    @Parcelize
    data class Update(val name: String, val changelog: String, val timestamp: String, val assetUrl: String, val assetName: String, val releaseUrl: String): Parcelable

}