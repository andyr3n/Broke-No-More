package com.example.broke_no_more.database

data class CryptoResponse(
    val data: List<CryptoData>
)

data class CryptoData(
    val id: Int,
    val name: String,
    val symbol: String,
    val quote: Map<String, Quote>
)

data class Quote(
    val price: Double
)
