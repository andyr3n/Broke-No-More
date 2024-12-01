package com.example.broke_no_more.ui.profile

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.DialogEditFieldBinding
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

    inner class Slide1ViewHolder(private val binding: ItemProfileSlide1Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Load and display the "Saving For" value
            binding.textViewSavingFor.text =
                sharedPreferences.getString("saving_for", "Add your goal !")

            // Load and display the "Wallet" value
            binding.textViewWallet.text =
                sharedPreferences.getString("wallet", "How much do you have ?")

            // Load and display the profile image
            val imagePath = sharedPreferences.getString("profile_image_path", null)
            if (imagePath != null) {
                val file = File(imagePath)
                if (file.exists()) {
                    binding.imageViewProfile.setImageURI(Uri.fromFile(file))
                } else {
                    binding.imageViewProfile.setImageResource(R.drawable.ic_profile)
                }
            } else {
                binding.imageViewProfile.setImageResource(R.drawable.ic_profile)
            }

            // Handle Profile Image Click
            binding.imageViewProfile.setOnClickListener {
                listener.onChangeImageClicked()
            }

            // Handle "Saving For" Field Click
            binding.textViewSavingFor.setOnClickListener {
                showEditDialog(
                    title = "Edit Saving For",
                    currentValue = binding.textViewSavingFor.text.toString(),
                    onSave = { newValue ->
                        binding.textViewSavingFor.text = newValue
                        sharedPreferences.edit().putString("saving_for", newValue).apply()
                        Toast.makeText(context, "Saving For updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Handle "Wallet" Field Click
            binding.textViewWallet.setOnClickListener {
                showEditDialog(
                    title = "Edit Wallet",
                    currentValue = binding.textViewWallet.text.toString(),
                    onSave = { newValue ->
                        binding.textViewWallet.text = newValue
                        sharedPreferences.edit().putString("wallet", newValue).apply()
                        Toast.makeText(context, "Wallet updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    inner class Slide2ViewHolder(private val binding: ItemProfileSlide2Binding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            // Load and display personal details
            binding.textViewName.text = sharedPreferences.getString("name", "Enter your name")
            binding.textViewAge.text = sharedPreferences.getString("age", "Enter your age")
            binding.textViewJob.text = sharedPreferences.getString("job", "Enter your job")
            binding.textViewHobby.text = sharedPreferences.getString("hobby", "Enter your hobby")


            // Handle "Name" Field Click
            binding.textViewName.setOnClickListener {
                showEditDialog(
                    title = "Edit Name",
                    currentValue = binding.textViewName.text.toString(),
                    onSave = { newValue ->
                        binding.textViewName.text = newValue
                        sharedPreferences.edit().putString("name", newValue).apply()
                        Toast.makeText(context, "Name updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Handle "Age" Field Click
            binding.textViewAge.setOnClickListener {
                showEditDialog(
                    title = "Edit Age",
                    currentValue = binding.textViewAge.text.toString(),
                    inputType = InputType.TYPE_CLASS_NUMBER,
                    onSave = { newValue ->
                        // Optional: Validate the age input here
                        binding.textViewAge.text = newValue
                        sharedPreferences.edit().putString("age", newValue).apply()
                        Toast.makeText(context, "Age updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Handle "Job" Field Click
            binding.textViewJob.setOnClickListener {
                showEditDialog(
                    title = "Edit Job",
                    currentValue = binding.textViewJob.text.toString(),
                    onSave = { newValue ->
                        binding.textViewJob.text = newValue
                        sharedPreferences.edit().putString("job", newValue).apply()
                        Toast.makeText(context, "Job updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Handle "Hobby" Field Click
            binding.textViewHobby.setOnClickListener {
                showEditDialog(
                    title = "Edit Hobby",
                    currentValue = binding.textViewHobby.text.toString(),
                    onSave = { newValue ->
                        binding.textViewHobby.text = newValue
                        sharedPreferences.edit().putString("hobby", newValue).apply()
                        Toast.makeText(context, "Hobby updated!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

        }
    }

    interface ProfileFragmentListener {
        fun onChangeImageClicked()
    }


    private fun showEditDialog(
        title: String,
        currentValue: String,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        onSave: (String) -> Unit
    ) {
        // Inflate the custom dialog layout using View Binding
        val dialogBinding = DialogEditFieldBinding.inflate(LayoutInflater.from(context))

        // Set the current value in the EditText
        dialogBinding.editTextField.setText(currentValue)
        dialogBinding.editTextField.inputType = inputType

        // Build and display the AlertDialog
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { dialog, _ ->
                val newValue = dialogBinding.editTextField.text.toString().trim()
                if (newValue.isNotEmpty()) {
                    onSave(newValue)
                } else {
                    Toast.makeText(context, "Value cannot be empty!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}
