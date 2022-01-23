package com.kieronquinn.app.taptap.service.retrofit

import com.google.gson.JsonObject
import com.kieronquinn.app.taptap.BuildConfig
import com.kieronquinn.app.taptap.models.phonespecs.SearchResponse
import com.kieronquinn.app.taptap.models.phonespecs.SpecResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PhoneSpecsService {

    companion object {
        private fun getSearchBody(): JsonObject {
            return JsonObject().apply {
                addProperty("category", "all")
            }
        }
    }

    @POST("api/product/search")
    @Headers("Content-Type: application/json")
    fun searchModel(@Query("query") model: String, @Body body: JsonObject = getSearchBody()): Call<SearchResponse>

    @GET("api/product/get/{id}")
    fun getModelSpecs(@Path("id") id: String): Call<SpecResponse>

}

fun createPhoneSpecsService(): PhoneSpecsService? {
    val baseUrl = BuildConfig.TECHSPECS_BASE_URL
    val apiKey = BuildConfig.TECHSPECS_API_KEY
    if(baseUrl == "null" || apiKey == "null") return null
    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(PhoneSpecsInterceptor(apiKey))
        .build()
    return Retrofit.Builder()
        .client(okHttpClient)
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .run {
            create(PhoneSpecsService::class.java)
        }
}

class PhoneSpecsInterceptor(private val apiKey: String): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Accept-Encoding", "gzip, deflate")
            .addHeader("x-blobr-key", apiKey)
            .build()
        return chain.proceed(request)
    }

}