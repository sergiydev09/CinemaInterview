package com.cinema.core.ui.navigation

object DeeplinkScheme {
    private const val SCHEME = "cinema"

    fun buildBasePath(path: String): String = "$SCHEME://$path"

    fun buildDeeplink(path: String, parameters: Map<String, String> = emptyMap()): String {
        val base = "$SCHEME://$path"
        if (parameters.isEmpty()) return base
        val query = parameters.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$base?$query"
    }
}
