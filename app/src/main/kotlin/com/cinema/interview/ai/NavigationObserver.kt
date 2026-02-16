package com.cinema.interview.ai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun NavigationObserver(
    navController: NavHostController,
    onScreenChanged: (String) -> Unit
) {
    val backStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(backStackEntry) {
        val uri = backStackEntry?.destination?.route
            ?.substringAfterLast(".")
            ?.substringBefore("/")
            ?.removeSuffix("Route")
            ?.fold(StringBuilder()) { sb, c ->
                if (c.isUpperCase() && sb.isNotEmpty()) sb.append('_')
                sb.append(c.lowercase())
            }
            ?.toString()
            ?: return@LaunchedEffect
        onScreenChanged(uri)
    }
}
