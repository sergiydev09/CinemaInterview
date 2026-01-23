package com.cinema.movies.ui.navigation

object MovieNavigationDeeplink {
    private const val SCHEME = "cinema"

    fun list() = "$SCHEME://movies"

    fun detail(movieId: Int) = "$SCHEME://movie/$movieId"
}
