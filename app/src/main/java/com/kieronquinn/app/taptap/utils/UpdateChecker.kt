package com.kieronquinn.app.taptap.utils

import android.os.Handler
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.utils.github.GitHubReleaseResponse
import kotlinx.android.parcel.Parcelize
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

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

    var newUpdate: MutableLiveData<Update?> = MutableLiveData(null)

    /*
     *  Checks for an update and returns true if successful, false if not
     */
    fun getLatestRelease(callback: (Boolean, UpdateChecker) -> Unit) {
        getReleaseList { gitHubReleaseResponse ->
            if(gitHubReleaseResponse == null) callback.invoke(false, this)
            else {
                val currentTag = gitHubReleaseResponse.tagName
                if(currentTag != BuildConfig.TAG_NAME){
                    //New update available!
                    val asset = gitHubReleaseResponse.assets!!.firstOrNull { it.name!!.endsWith(".apk") }
                    newUpdate.postValue(Update(gitHubReleaseResponse.name!!, gitHubReleaseResponse.body!!, gitHubReleaseResponse.publishedAt!!, asset?.browserDownloadUrl ?: "https://github.com/KieronQuinn/TapTap/releases", asset?.name ?: "TapTap.apk"))
                }
                //Allow the value to be updated first
                Handler().postDelayed({
                    callback.invoke(true, this)
                }, 0)
            }
        }
    }

    private fun getReleaseList(callback: ((GitHubReleaseResponse?) -> Unit)) {
        val service: GitHubService = retrofit.create(GitHubService::class.java)
        service.getReleaseList().enqueue(object: Callback<GitHubReleaseResponse>{
            override fun onFailure(call: Call<GitHubReleaseResponse>, t: Throwable) {
                callback.invoke(null)
            }

            override fun onResponse(call: Call<GitHubReleaseResponse>, response: Response<GitHubReleaseResponse>) {
                callback.invoke(response.body())
            }

        })
    }

    interface GitHubService {
        @GET("releases/latest")
        fun getReleaseList(): Call<GitHubReleaseResponse>
    }

    @Parcelize
    data class Update(val name: String, val changelog: String, val timestamp: String, val assetUrl: String, val assetName: String): Parcelable

}