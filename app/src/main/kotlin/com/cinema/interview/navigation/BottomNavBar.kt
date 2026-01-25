package com.cinema.interview.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cinema.home.ui.navigation.HomeRoute
import com.cinema.interview.R
import com.cinema.movies.ui.navigation.MoviesRoute
import com.cinema.people.ui.navigation.PeopleRoute

data class BottomNavItem<T : Any>(
    val route: T,
    val icon: ImageVector,
    val labelResId: Int
)

val bottomNavItems = listOf(
    BottomNavItem(HomeRoute, Icons.Default.Home, R.string.nav_home),
    BottomNavItem(MoviesRoute, Icons.Default.PlayArrow, R.string.nav_movies),
    BottomNavItem(PeopleRoute, Icons.Default.Person, R.string.nav_people)
)

@Composable
fun BottomNavBar(
    navController: NavController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.route::class) == true
    }

    if (showBottomBar) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentDestination?.hasRoute(item.route::class) == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = stringResource(item.labelResId)
                        )
                    },
                    label = { Text(stringResource(item.labelResId)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
