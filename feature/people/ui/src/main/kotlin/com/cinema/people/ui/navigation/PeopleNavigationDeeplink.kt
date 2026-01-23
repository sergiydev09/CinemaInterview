package com.cinema.people.ui.navigation

object PeopleNavigationDeeplink {
    private const val SCHEME = "cinema"

    fun list() = "$SCHEME://people"

    fun detail(personId: Int) = "$SCHEME://person/$personId"
}
