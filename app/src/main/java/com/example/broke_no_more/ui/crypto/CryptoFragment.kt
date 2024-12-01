package com.example.broke_no_more.ui.crypto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broke_no_more.API.RetrofitInstance
import com.example.broke_no_more.database.CryptoDatabase
import com.example.broke_no_more.databinding.FragmentCryptoBinding
import com.example.broke_no_more.entity.CryptoTransaction
import com.example.broke_no_more.repository.CryptoRepository

class CryptoFragment : Fragment() {

    private var _binding: FragmentCryptoBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CryptoViewModel
    private lateinit var adapter: CryptoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCryptoBinding.inflate(inflater, container, false)

        // Setup dependencies
        val api = RetrofitInstance.create()
        val dao = CryptoDatabase.getDatabase(requireContext()).cryptoDao()
        val repository = CryptoRepository(api, dao)
        val factory = CryptoViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CryptoViewModel::class.java]

        // Setup RecyclerView with click listener
        adapter = CryptoAdapter(emptyList()) { selectedCrypto ->
            updateCryptoWebView(selectedCrypto)
        }
        binding.rvCrypto.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCrypto.adapter = adapter

        // Configure WebView
        setupCryptoWebView()

        // Observe cryptocurrency data
        viewModel.allCryptoTransactions.observe(viewLifecycleOwner) { transactions ->
            // Populate RecyclerView with all cryptocurrencies, including Bitcoin
            adapter.updateData(transactions)

            // Find Bitcoin transaction for initial load
            val bitcoinTransaction = transactions.find { it.name.equals("Bitcoin", ignoreCase = true) }

            // Set initial chart to Bitcoin if available
            bitcoinTransaction?.let { updateCryptoWebView(it) }

            // Optionally, you can set the WebView to the first item if Bitcoin isn't present
            /*
            if (bitcoinTransaction == null && transactions.isNotEmpty()) {
                updateCryptoWebView(transactions[0])
            }
            */
        }

        // Fetch data
        viewModel.fetchCryptocurrencies()

        return binding.root
    }

    private fun setupCryptoWebView() {
        val webView = binding.cryptoWebView
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        webView.webViewClient = WebViewClient()
    }

    private fun updateCryptoWebView(cryptoTransaction: CryptoTransaction) {
        val symbol = cryptoTransaction.symbol.toUpperCase()
        val tradingViewUrl = "https://s.tradingview.com/widgetembed/?frameElementId=tradingview_76d87&symbol=${symbol}USD&interval=D&hidesidetoolbar=1&hidetoptoolbar=1&symboledit=1&saveimage=1&toolbarbg=F1F3F6&studies=[]&hideideas=1&theme=Light&style=1&timezone=Etc/UTC&studies_overrides={}&overrides={}&enabled_features=[]&disabled_features=[]&locale=en&utm_source=coinmarketcap.com&utm_medium=widget&utm_campaign=chart&utm_term=${symbol}USDT"

        binding.cryptoWebView.loadUrl(tradingViewUrl)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
