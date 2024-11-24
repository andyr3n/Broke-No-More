package com.example.broke_no_more.repository

import com.example.broke_no_more.API.CoinMarketCapApi
import com.example.broke_no_more.database.CryptoDao
import com.example.broke_no_more.entity.CryptoTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CryptoRepository(
    private val api: CoinMarketCapApi,
    private val cryptoDao: CryptoDao
) {

    val allCryptoTransactions = cryptoDao.getAllCryptoTransactions()

    suspend fun fetchAndSaveCryptocurrencies() {
        withContext(Dispatchers.IO) {
            val response = api.getCryptocurrencies().execute()
            if (response.isSuccessful) {
                val cryptoList = response.body()?.data?.map {
                    CryptoTransaction(
                        id = it.id.toLong(),
                        name = it.name,
                        symbol = it.symbol,
                        date = System.currentTimeMillis().toString(),
                        amountInUSD = it.quote["USD"]?.price ?: 0.0,
                        amountInCrypto = 0.0 // Optional, calculate if needed
                    )
                }
                cryptoList?.let { cryptoDao.insertAll(it) }
            }
        }
    }
}
