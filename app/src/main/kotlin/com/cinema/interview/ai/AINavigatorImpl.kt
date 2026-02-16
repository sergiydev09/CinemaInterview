package com.cinema.interview.ai

import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import com.cinema.core.ai.domain.manager.AINavigator
import com.cinema.core.ui.navigation.DeeplinkScheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AINavigatorImpl @Inject constructor() : AINavigator {

    private var navController: NavHostController? = null

    fun setNavController(controller: NavHostController) {
        navController = controller
    }

    override fun navigateTo(target: String, parameters: Map<String, String>) {
        val controller = navController ?: return

        val deeplink = DeeplinkScheme.buildDeeplink(target, parameters)
        Log.d(TAG, "Navigating via deeplink: $deeplink")

        val intent = Intent(Intent.ACTION_VIEW, deeplink.toUri())
        controller.handleDeepLink(intent)
    }

    companion object {
        private const val TAG = "AINavigator"
    }
}
