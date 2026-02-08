package com.cinema.interview.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cinema.core.ai.ui.compose.AIAssistant
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.ui.R
import com.cinema.core.ui.compose.BottomNavBar
import com.cinema.core.ui.compose.BottomNavItem
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.home.ui.navigation.HomeRoute
import com.cinema.interview.ai.AINavigatorImpl
import com.cinema.interview.ai.NavigationObserver
import com.cinema.interview.navigation.MainNavGraph
import com.cinema.interview.navigation.SessionNavigator
import com.cinema.movies.ui.navigation.MoviesRoute
import com.cinema.people.ui.navigation.PeopleRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PrivateActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var sessionNavigator: SessionNavigator
    @Inject lateinit var aiNavigatorImpl: AINavigatorImpl

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

                LaunchedEffect(Unit) {
                    aiNavigatorImpl.setNavController(navController)
                }

                AIAssistant { aiViewModel, aiFab, aiInputBar ->
                    NavigationObserver(
                        navController = navController,
                        onScreenChanged = aiViewModel::setCurrentScreen
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            Box {
                                BottomNavBar(
                                    navController, items = listOf(
                                        BottomNavItem(HomeRoute, Icons.Default.Home, R.string.nav_home),
                                        BottomNavItem(MoviesRoute, Icons.Default.PlayArrow, R.string.nav_movies),
                                        BottomNavItem(PeopleRoute, Icons.Default.Person, R.string.nav_people)
                                    )
                                )
                                aiInputBar()
                            }
                        },
                        floatingActionButton = aiFab
                    ) { paddingValues ->
                        MainNavGraph(
                            navController = navController,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }

    private fun setupSessionManager() {
        sessionManager.setOnSessionExpired {
            sessionManager.logout()
            navigateToLogin()
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        sessionManager.resetInactivityTimer()
        return super.dispatchTouchEvent(ev)
    }

    private fun navigateToLogin() {
        sessionNavigator.navigateToLogin(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sessionManager.setOnSessionExpired(null)
    }
}
