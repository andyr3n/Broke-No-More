package com.example.broke_no_more.ui.crypto

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.broke_no_more.entity.CryptoTransaction
import com.example.broke_no_more.repository.CryptoRepository
import kotlinx.coroutines.launch

class CryptoViewModel(private val repository: CryptoRepository) : ViewModel() {

    val allCryptoTransactions: LiveData<List<CryptoTransaction>> = repository.allCryptoTransactions

    fun fetchCryptocurrencies() {
        viewModelScope.launch {
            repository.fetchAndSaveCryptocurrencies()
        }
    }
}
