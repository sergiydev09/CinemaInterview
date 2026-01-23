package com.cinema.interview.presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cinema.core.domain.session.SessionManager
import com.cinema.interview.R
import com.cinema.login.ui.LoginNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Public Activity that hosts the login flow.
 * This activity is displayed when the user is not authenticated.
 * Contains a container for the LoginFragment.
 */
@AndroidEntryPoint
class PublicActivity : AppCompatActivity(), LoginNavigator {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (sessionManager.isSessionActive()) {
            navigateToPrivateArea()
            return
        }

        setContentView(R.layout.activity_public)
    }

    override fun onLoginSuccess() {
        navigateToPrivateArea()
    }

    private fun navigateToPrivateArea() {
        val intent = Intent(this, PrivateActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
