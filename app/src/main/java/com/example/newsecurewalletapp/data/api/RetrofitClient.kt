package com.example.newsecurewalletapp.data.api

import com.example.newsecurewalletapp.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val baseUrl: String
        get() {
            val configUrl = BuildConfig.SERVER_API_URL
            return if (configUrl.isNotBlank()) configUrl else "http://10.0.2.2:8080/"
        }

    val api: DomainRegistrationApi by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DomainRegistrationApi::class.java)
    }
}
