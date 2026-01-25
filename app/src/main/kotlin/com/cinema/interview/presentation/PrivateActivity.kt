package com.cinema.interview.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cinema.core.domain.session.SessionCallback
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.interview.navigation.BottomNavBar
import com.cinema.interview.navigation.MainNavGraph
import com.cinema.interview.navigation.SessionNavigator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrivateActivity : ComponentActivity(), SessionCallback {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var sessionNavigator: SessionNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!sessionManager.isSessionActive()) {
            navigateToLogin()
            return
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setupSessionManager()

        setContent {
            CinemaTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavBar(navController) }
                ) { paddingValues ->
                    MainNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
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
        sessionNavigator.navigateToLogin(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setSessionCallback(null)
    }
}
