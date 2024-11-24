package com.example.broke_no_more.API

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://pro-api.coinmarketcap.com/" // Base URL for CoinMarketCap API

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(): CoinMarketCapApi {
        return retrofit.create(CoinMarketCapApi::class.java)
    }
}
