package com.example.broke_no_more

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.broke_no_more.assistant.AIChatDialogFragment
import com.example.broke_no_more.databinding.ActivityMainBinding
import com.example.broke_no_more.ui.profile.ProfileActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)

        val navigateToHome = intent.getBooleanExtra("navigateToHome", false)
        if (navigateToHome) {
            navController.navigate(R.id.nav_home)
        }

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_expense_report,
                R.id.nav_savings_goal,
                R.id.nav_crypto,
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> navigateToTab(navController, R.id.nav_home)
                R.id.nav_expense_report -> navigateToTab(navController, R.id.nav_expense_report)
                R.id.nav_savings_goal -> navigateToTab(navController, R.id.nav_savings_goal)
                R.id.nav_crypto -> navigateToTab(navController, R.id.nav_crypto)
                else -> false
            }
        }

        binding.appBarMain.fab.setOnClickListener {
            val aiChatDialog = AIChatDialogFragment()
            aiChatDialog.show(supportFragmentManager, "AIChatDialogFragment")
        }
    }

    private fun navigateToTab(navController: androidx.navigation.NavController, tabId: Int): Boolean {
        navController.popBackStack(tabId, false)
        navController.navigate(tabId)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        if (navController.currentDestination?.id != R.id.nav_home) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        } else {
            super.onBackPressed()
        }
    }
}
