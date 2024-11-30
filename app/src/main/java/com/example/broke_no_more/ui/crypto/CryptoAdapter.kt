package com.example.broke_no_more.ui.crypto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.entity.CryptoTransaction

class CryptoAdapter(
    private var cryptoList: List<CryptoTransaction>,
    private val onItemClick: (CryptoTransaction) -> Unit
) : RecyclerView.Adapter<CryptoAdapter.CryptoViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class CryptoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.cryptoName)
        val symbol: TextView = itemView.findViewById(R.id.cryptoSymbol)
        val price: TextView = itemView.findViewById(R.id.cryptoPrice)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Update selected position
                    notifyItemChanged(selectedPosition)
                    selectedPosition = position
                    notifyItemChanged(selectedPosition)

                    onItemClick(cryptoList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_crypto, parent, false)
        return CryptoViewHolder(view)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = cryptoList[position]
        holder.name.text = crypto.name
        holder.symbol.text = crypto.symbol
        holder.price.text = "$${String.format("%.2f", crypto.amountInUSD)}"

        // Highlight selected item
        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.color.selected_item_background) // Define this color in your resources
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
        }
    }

    override fun getItemCount() = cryptoList.size

    fun updateData(newCryptoList: List<CryptoTransaction>) {
        cryptoList = newCryptoList
        notifyDataSetChanged()
    }
}
