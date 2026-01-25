package com.cinema.home.ui.navigation

import androidx.navigation.navDeepLink
import com.cinema.core.ui.navigation.DeeplinkScheme
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

object HomeDeeplinks {
    val home = listOf(
        navDeepLink<HomeRoute>(basePath = DeeplinkScheme.buildBasePath("home"))
    )
}
