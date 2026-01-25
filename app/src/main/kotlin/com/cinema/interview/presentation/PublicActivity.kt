package com.cinema.interview.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.interview.navigation.SessionNavigator
import com.cinema.login.ui.feature.LoginScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PublicActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionNavigator: SessionNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sessionManager.isSessionActive()) {
            navigateToPrivateArea()
            return
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        setContent {
            CinemaTheme {
                LoginScreen(
                    onLoginSuccess = ::navigateToPrivateArea
                )
            }
        }
    }

    private fun navigateToPrivateArea() {
        sessionNavigator.navigateToPrivateArea(this)
    }
}
