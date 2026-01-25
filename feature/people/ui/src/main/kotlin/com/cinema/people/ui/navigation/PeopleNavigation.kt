package com.cinema.people.ui.navigation

import androidx.navigation.navDeepLink
import com.cinema.core.ui.navigation.DeeplinkScheme
import kotlinx.serialization.Serializable

@Serializable
data object PeopleRoute

@Serializable
data class PersonDetailRoute(val personId: Int)

object PeopleDeeplinks {
    val people = listOf(
        navDeepLink<PeopleRoute>(basePath = DeeplinkScheme.buildBasePath("people"))
    )

    val personDetail = listOf(
        navDeepLink<PersonDetailRoute>(basePath = DeeplinkScheme.buildBasePath("person"))
    )
}
