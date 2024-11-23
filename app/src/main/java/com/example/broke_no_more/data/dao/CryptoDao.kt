package com.example.broke_no_more.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.broke_no_more.entity.CryptoTransaction

@Dao
interface CryptoDao {

    @Query("SELECT * FROM crypto_table ORDER BY id ASC")
    fun getAllCryptoTransactions(): LiveData<List<CryptoTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<CryptoTransaction>)
}
