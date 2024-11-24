package com.example.broke_no_more.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crypto_table")
data class CryptoTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val date: String,
    val amountInUSD: Double,
    val amountInCrypto: Double
)
