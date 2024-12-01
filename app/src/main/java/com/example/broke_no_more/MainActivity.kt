package com.example.broke_no_more

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.broke_no_more.assistant.AIChatDialogFragment
import com.example.broke_no_more.databinding.ActivityMainBinding
import com.example.broke_no_more.ui.profile.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar
        setSupportActionBar(binding.appBarMain.toolbar)

        // Get NavController
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Handle intent extras for navigation
        val navigateToHome = intent.getBooleanExtra("navigateToHome", false)
        if (navigateToHome) {
            navController.navigate(R.id.nav_home)
        }

        // Configure top-level destinations for the app bar
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_expense_report, R.id.nav_savings_goal, R.id.nav_crypto, R.id.nav_profile)
        )

        // Set up action bar with NavController
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Set up BottomNavigationView
        val bottomNavigationView: BottomNavigationView = binding.bottomNavigation
        bottomNavigationView.setupWithNavController(navController)

        // Set up FloatingActionButton for Chatbot
        binding.appBarMain.fab.setOnClickListener {
            val aiChatDialog = AIChatDialogFragment()
            aiChatDialog.show(supportFragmentManager, "AIChatDialogFragment")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val profileFragment = ProfileFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_content_main, profileFragment)
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
