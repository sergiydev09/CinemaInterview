package com.cinema.interview.presentation

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.cinema.core.domain.session.SessionCallback
import com.cinema.core.domain.session.SessionManager
import com.cinema.interview.R
import com.cinema.interview.databinding.ActivityPrivateBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Private Activity that hosts the main app content after authentication.
 * Contains the bottom navigation and NavHost for feature fragments.
 * Implements SessionCallback to handle session expiration.
 */
@AndroidEntryPoint
class PrivateActivity : AppCompatActivity(), SessionCallback {

    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityPrivateBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if session is valid
        if (!sessionManager.isSessionActive()) {
            navigateToLogin()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupNavigation()
        setupSessionManager()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { view, insets ->
            view.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { view, insets ->
            view.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupSessionManager() {
        sessionManager.setSessionCallback(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        sessionManager.resetInactivityTimer()
        return super.dispatchTouchEvent(ev)
    }

    override fun onSessionExpired() {
        runOnUiThread {
            sessionManager.logout()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, PublicActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setSessionCallback(null)
    }
}
