package com.example.broke_no_more.ui.crypto

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.broke_no_more.API.RetrofitInstance
import com.example.broke_no_more.database.CryptoDatabase
import com.example.broke_no_more.databinding.FragmentCryptoBinding
import com.example.broke_no_more.entity.CryptoTransaction
import com.example.broke_no_more.repository.CryptoRepository
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

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

        // Setup RecyclerView
        adapter = CryptoAdapter(emptyList())
        binding.rvCrypto.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCrypto.adapter = adapter

        // Observe cryptocurrency data
        viewModel.allCryptoTransactions.observe(viewLifecycleOwner) { transactions ->
            val bitcoinTransaction = transactions.find { it.name == "Bitcoin" }
            val otherTransactions = transactions.filter { it.name != "Bitcoin" }

            // Update RecyclerView with all other cryptos
            adapter.updateData(otherTransactions)

            // Update Bitcoin graph
            bitcoinTransaction?.let { updateBitcoinGraph(it) }

            // Update Other Cryptocurrencies graph
            updateOtherCryptosGraph(otherTransactions)
        }

        // Fetch data
        viewModel.fetchCryptocurrencies()

        return binding.root
    }

    private fun updateBitcoinGraph(bitcoinTransaction: CryptoTransaction) {
        val entries = listOf(
            Entry(0f, bitcoinTransaction.amountInUSD.toFloat())
        )

        val dataSet = LineDataSet(entries, "Bitcoin Price").apply {
            color = Color.YELLOW
            valueTextColor = Color.WHITE
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.YELLOW)
        }
        val xAxis = binding.bitcoinChart.xAxis
        val axisLeft = binding.bitcoinChart.axisLeft
        val axisRight = binding.bitcoinChart.axisRight
        xAxis.textColor = Color.WHITE
        axisLeft.textColor = Color.WHITE
        axisRight.textColor = Color.WHITE

        val lineData = LineData(dataSet)
        binding.bitcoinChart.apply {
            data = lineData
            description = Description().apply {
                text = "Bitcoin Price Trend"
                textColor = Color.WHITE
            }
            animateX(1000)
            invalidate() // Refresh the chart
        }
    }

    private fun updateOtherCryptosGraph(transactions: List<CryptoTransaction>) {
        val entries = transactions.mapIndexed { index, transaction ->
            Entry(index.toFloat(), transaction.amountInUSD.toFloat())
        }

        val dataSet = LineDataSet(entries, "Other Cryptocurrencies").apply {
            color = Color.BLUE
            valueTextColor = Color.WHITE
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.RED)
        }
        val xAxis = binding.otherCryptoChart.xAxis
        val axisLeft = binding.otherCryptoChart.axisLeft
        val axisRight = binding.otherCryptoChart.axisRight
        xAxis.textColor = Color.WHITE
        axisLeft.textColor = Color.WHITE
        axisRight.textColor = Color.WHITE

        val lineData = LineData(dataSet)
        binding.otherCryptoChart.apply {
            data = lineData
            description = Description().apply {
                text = "Other Cryptos Trend"
                textColor = Color.WHITE
            }
            animateX(1000)
            invalidate() // Refresh the chart
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
