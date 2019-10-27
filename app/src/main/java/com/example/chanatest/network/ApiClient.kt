package com.example.chanatest.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiClient {
    val apiPlace : ApiService

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout (15, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(okHttpClient)
            .build()
        apiPlace = retrofit.create(ApiService::class.java)
    }

    companion object{
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/"
        private const val BASE_URL2 = "https://s3.eu-central-1.amazonaws.com/wunderfleet-recruiting-dev/"
    }
}