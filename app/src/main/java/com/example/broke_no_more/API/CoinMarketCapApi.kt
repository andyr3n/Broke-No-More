package com.example.broke_no_more.API

import com.example.broke_no_more.database.CryptoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers

interface CoinMarketCapApi {

    @Headers("X-CMC_PRO_API_KEY:69cc8602-937b-42de-8396-38115145ce01")
    @GET("v1/cryptocurrency/listings/latest")
    fun getCryptocurrencies(): Call<CryptoResponse>
}
