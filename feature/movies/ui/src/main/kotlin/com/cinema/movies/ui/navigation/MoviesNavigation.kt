package com.cinema.movies.ui.navigation

import androidx.navigation.navDeepLink
import com.cinema.core.ui.navigation.DeeplinkScheme
import kotlinx.serialization.Serializable

@Serializable
data object MoviesRoute

@Serializable
data class MovieDetailRoute(val movieId: Int)

object MoviesDeeplinks {
    val movies = listOf(
        navDeepLink<MoviesRoute>(basePath = DeeplinkScheme.buildBasePath("movies"))
    )

    val movieDetail = listOf(
        navDeepLink<MovieDetailRoute>(basePath = DeeplinkScheme.buildBasePath("movie"))
    )
}
