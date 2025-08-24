package com.azgear.sendmoney

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.azgear.sendmoney.databinding.ActivityMainWithBottomNavBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainWithBottomNavBinding
    private lateinit var navController: NavController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        Log.d("MainActivity", "onCreate - Setting up single layout with bottom nav")
        
        // Use single layout approach
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_with_bottom_nav)
        
        setupNavigation()
        setupWindowInsets()
    }
    
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        Log.d("MainActivity", "Setting up navigation")
        
        // Setup bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Handle bottom nav visibility based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("MainActivity", "Navigation destination changed to: ${resources.getResourceEntryName(destination.id)}")
            
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.sendMoneyFragment, R.id.confirmationFragment -> {
                    Log.d("MainActivity", "Hiding bottom navigation")
                    // Hide bottom navigation for auth flow and full-screen fragments
                    binding.bottomNavigation.visibility = View.GONE
                    supportActionBar?.hide()
                }
                R.id.homeFragment, R.id.transactionsFragment, R.id.profileFragment -> {
                    Log.d("MainActivity", "Showing bottom navigation")
                    // Show bottom navigation for main app fragments
                    binding.bottomNavigation.visibility = View.VISIBLE
                    supportActionBar?.hide() // Hide action bar in favor of bottom nav
                }
                else -> {
                    Log.d("MainActivity", "Other destination")
                    // Other fragments - show action bar, hide bottom nav
                    binding.bottomNavigation.visibility = View.GONE
                    supportActionBar?.show()
                    setupActionBarWithNavController(navController)
                }
            }
        }
    }
    
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}