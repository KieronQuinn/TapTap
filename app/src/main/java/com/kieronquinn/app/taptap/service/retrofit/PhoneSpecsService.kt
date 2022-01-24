package com.kieronquinn.app.taptap.service.retrofit

import com.kieronquinn.app.taptap.models.phonespecs.SpecResponse
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PhoneSpecsService {

    @GET("specs")
    fun getModelSpecs(@Query("model") model: String): Call<SpecResponse>

}

private const val BASE_URL = "https://kieronquinn.co.uk/api/"

fun createPhoneSpecsService(): PhoneSpecsService {
    val okHttpClient = OkHttpClient.Builder()
        .build()
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .run {
            create(PhoneSpecsService::class.java)
        }
}