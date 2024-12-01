package com.example.broke_no_more.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.broke_no_more.R
import com.example.broke_no_more.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: ProfilePagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view: View = binding.root

        setupViewPager()

        return view
    }

    private fun setupViewPager() {
        pagerAdapter = ProfilePagerAdapter()
        binding.viewPagerProfile.adapter = pagerAdapter

        // Optional: Set page change callbacks or other configurations
        binding.viewPagerProfile.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Handle page change if needed
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPagerProfile.unregisterOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {})
        _binding = null
    }
}
