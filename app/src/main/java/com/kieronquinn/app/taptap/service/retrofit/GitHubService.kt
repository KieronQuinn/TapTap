package com.kieronquinn.app.taptap.service.retrofit

import com.kieronquinn.app.taptap.models.github.GitHubRelease
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface GitHubService {

    @GET("releases")
    fun getReleases(): Call<Array<GitHubRelease>>

}

fun createGitHubService(): GitHubService =
    Retrofit.Builder()
        .baseUrl("https://api.github.com/repos/KieronQuinn/TapTap/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .run {
            create(GitHubService::class.java)
        }