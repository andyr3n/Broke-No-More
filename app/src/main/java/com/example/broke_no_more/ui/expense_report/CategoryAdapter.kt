package com.example.broke_no_more.ui.expense_report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var categories: List<Pair<String, Double>> = emptyList()

    fun setCategories(data: List<Pair<String, Double>>) {
        categories = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val (category, total) = categories[position]
        holder.categoryName.text = category
        holder.categoryTotal.text = String.format("$%.2f", total)
    }

    override fun getItemCount(): Int = categories.size

    class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.categoryName)
        val categoryTotal: TextView = view.findViewById(R.id.categoryTotal)
    }
}
