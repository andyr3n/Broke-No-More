package com.example.broke_no_more.ui.profile

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.broke_no_more.databinding.ActivityProfileBinding
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity(), ProfilePagerAdapter.ProfileFragmentListener {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var pagerAdapter: ProfilePagerAdapter
    private lateinit var sharedPreferences: SharedPreferences

    private var imageUri: Uri? = null
    private var imagePath: String? = null

    private var tabLayoutMediator: TabLayoutMediator? = null

    // Camera and Gallery Launchers
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri?.let {
                val savedPath = copyImageToInternalStorage(it)
                // Update SharedPreferences
                sharedPreferences.edit().putString("profile_image_path", savedPath).apply()
                // Notify adapter to refresh views
                pagerAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            imageUri = result.data?.data
            imageUri?.let {
                val savedPath = copyImageToInternalStorage(it)
                // Update SharedPreferences
                sharedPreferences.edit().putString("profile_image_path", savedPath).apply()
                // Notify adapter to refresh views
                pagerAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Profile image updated!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)

        setupViewPager()
    }

    private fun setupViewPager() {
        pagerAdapter = ProfilePagerAdapter(this, sharedPreferences, this)
        binding.viewPagerProfile.adapter = pagerAdapter

        // Apply PageTransformer
        binding.viewPagerProfile.setPageTransformer(ZoomOutPageTransformer())

        // Initialize TabLayoutMediator
        tabLayoutMediator = TabLayoutMediator(binding.tabLayoutIndicators, binding.viewPagerProfile) { tab, position ->
            when (position) {
                0 -> tab.text = "Profile"
                1 -> tab.text = "Details"
                else -> tab.text = "Tab ${position + 1}"
            }
        }
        tabLayoutMediator?.attach()

        // Optional: Set page change callbacks or other configurations
        binding.viewPagerProfile.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Handle page change if needed
            }
        })
    }

    override fun onChangeImageClicked() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select an option")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        launchCamera()
                    } else {
                        requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
                    }
                }
                options[item] == "Choose from Gallery" -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    galleryLauncher.launch(intent)
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    // Launch Camera Intent
    private fun launchCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Profile Picture")
            put(MediaStore.Images.Media.DISPLAY_NAME, "profile_image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp") // Save path
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(cameraIntent)
    }

    // Copy Image to Internal Storage
    private fun copyImageToInternalStorage(uri: Uri): String {
        val fileName = "profile_image_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                launchCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to take a picture", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
    }

    override fun onDestroy() {
        super.onDestroy()
        tabLayoutMediator?.detach()
        binding.viewPagerProfile.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
    }
}
