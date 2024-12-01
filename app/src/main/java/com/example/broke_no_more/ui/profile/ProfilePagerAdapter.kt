package com.example.broke_no_more.ui.profile

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.ItemProfileSlide1Binding
import com.example.broke_no_more.databinding.ItemProfileSlide2Binding
import java.io.File

class ProfilePagerAdapter(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val listener: ProfileFragmentListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Total number of slides
    private val slideCount = 2

    override fun getItemCount(): Int = slideCount

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding = ItemProfileSlide1Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                Slide1ViewHolder(binding)
            }
            1 -> {
                val binding = ItemProfileSlide2Binding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                Slide2ViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is Slide1ViewHolder -> holder.bind()
            is Slide2ViewHolder -> holder.bind()
        }
    }

    // ViewHolder for Slide 1
    inner class Slide1ViewHolder(private val binding: ItemProfileSlide1Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Load saved data
            binding.editTextSavingFor.setText(sharedPreferences.getString("saving_for", "Travel"))
            binding.editTextWallet.setText(sharedPreferences.getString("wallet", "$1,200"))

            // Load profile image
            val imagePath = sharedPreferences.getString("profile_image_path", null)
            if (imagePath != null) {
                val file = File(imagePath)
                if (file.exists()) {
                    binding.imageViewProfile.setImageURI(Uri.fromFile(file))
                } else {
                    binding.imageViewProfile.setImageResource(R.drawable.ic_profile) // Default image
                }
            } else {
                binding.imageViewProfile.setImageResource(R.drawable.ic_profile) // Default image
            }

            // Handle Save Button
            binding.buttonSaveProfile.setOnClickListener {
                // Save data to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("saving_for", binding.editTextSavingFor.text.toString())
                    putString("wallet", binding.editTextWallet.text.toString())
                    apply()
                }
                Toast.makeText(context, "Profile Slide 1 saved!", Toast.LENGTH_SHORT).show()
            }

            // Handle Cancel Button
            binding.buttonCancelProfile.setOnClickListener {
                // Reload data to discard changes
                binding.editTextSavingFor.setText(sharedPreferences.getString("saving_for", "Travel"))
                binding.editTextWallet.setText(sharedPreferences.getString("wallet", "$1,200"))
            }

            // Handle ImageView Click
            binding.imageViewProfile.setOnClickListener {
                listener.onChangeImageClicked()
            }
        }
    }

    // ViewHolder for Slide 2
    inner class Slide2ViewHolder(private val binding: ItemProfileSlide2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Load saved data
            binding.editTextName.setText(sharedPreferences.getString("name", "John Doe"))
            binding.editTextAge.setText(sharedPreferences.getString("age", "30"))
            binding.editTextJob.setText(sharedPreferences.getString("job", "Software Engineer"))
            binding.editTextHobby.setText(sharedPreferences.getString("hobby", "Photography"))
            binding.editTextInterestedSavingFor.setText(sharedPreferences.getString("interested_saving_for", "• Savings for a new car"))
            binding.editTextInterestedWallet.setText(sharedPreferences.getString("interested_wallet", "• Upgrading wallet"))

            // Handle Save Button
            binding.buttonSaveProfile.setOnClickListener {
                // Save data to SharedPreferences
                with(sharedPreferences.edit()) {
                    putString("name", binding.editTextName.text.toString())
                    putString("age", binding.editTextAge.text.toString())
                    putString("job", binding.editTextJob.text.toString())
                    putString("hobby", binding.editTextHobby.text.toString())
                    putString("interested_saving_for", binding.editTextInterestedSavingFor.text.toString())
                    putString("interested_wallet", binding.editTextInterestedWallet.text.toString())
                    apply()
                }
                Toast.makeText(context, "Profile Slide 2 saved!", Toast.LENGTH_SHORT).show()
            }

            // Handle Cancel Button
            binding.buttonCancelProfile.setOnClickListener {
                // Reload data to discard changes
                binding.editTextName.setText(sharedPreferences.getString("name", "John Doe"))
                binding.editTextAge.setText(sharedPreferences.getString("age", "30"))
                binding.editTextJob.setText(sharedPreferences.getString("job", "Software Engineer"))
                binding.editTextHobby.setText(sharedPreferences.getString("hobby", "Photography"))
                binding.editTextInterestedSavingFor.setText(sharedPreferences.getString("interested_saving_for", "• Savings for a new car"))
                binding.editTextInterestedWallet.setText(sharedPreferences.getString("interested_wallet", "• Upgrading wallet"))
            }
        }
    }

    interface ProfileFragmentListener {
        fun onChangeImageClicked()
    }
}
