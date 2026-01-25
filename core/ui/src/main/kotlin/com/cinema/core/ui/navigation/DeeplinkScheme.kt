package com.cinema.core.ui.navigation

object DeeplinkScheme {
    private const val SCHEME = "cinema"

    fun buildBasePath(path: String): String = "$SCHEME://$path"
}
