package com.example.broke_no_more.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R

class ProfilePagerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Total number of slides
    private val slideCount = 2

    override fun getItemCount(): Int = slideCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_profile_slide1, parent, false)
                Slide1ViewHolder(view)
            }
            1 -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_profile_slide2, parent, false)
                Slide2ViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int = position

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Bind data if necessary
        // For static layouts, no binding is required here
    }

    // ViewHolder for Slide 1
    class Slide1ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views if you need to set data dynamically
    }

    // ViewHolder for Slide 2
    class Slide2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize views if you need to set data dynamically
    }
}
