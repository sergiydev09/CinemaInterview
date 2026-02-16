package com.cinema.movies.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.ai.domain.model.AIIntentDescriptor
import com.cinema.core.ai.domain.model.AIIntentParameter
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.ui.feature.detail.MovieDetailIntent
import com.cinema.movies.ui.feature.list.MoviesIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesAIIntentHandler @Inject constructor() : AIIntentHandler() {

    override val featureId: String = "movies"

    @Volatile
    private var currentMovies: List<Movie> = emptyList()

    @Volatile
    private var currentTimeWindow: TimeWindow = TimeWindow.DAY

    fun setScreenData(movies: List<Movie>, timeWindow: TimeWindow) {
        currentMovies = movies
        currentTimeWindow = timeWindow
        notifyScreenDataLoadCompleted()
    }

    override fun getScreenContext(): String? {
        if (currentMovies.isEmpty()) return null
        return buildString {
            val windowLabel = if (currentTimeWindow == TimeWindow.DAY) "today" else "this week"
            appendLine("Currently showing: trending movies $windowLabel")
            currentMovies.forEach { movie ->
                appendLine("id=${movie.id} \"${movie.title}\" (${movie.releaseDate.take(4)}) [${movie.genreNames.joinToString(",")}] rating=${movie.voteAverage}")
            }
        }.trimEnd()
    }

    override fun getDescriptors(): List<AIIntentDescriptor> = listOf(
        AIIntentDescriptor(
            intentId = "navigate_movies",
            description = "Navigate to the movies screen to browse trending movies",
            parameters = emptyList(),
            requiredScreen = null
        ),
        AIIntentDescriptor(
            intentId = "movies_trending_today",
            description = "Show trending movies today",
            parameters = emptyList(),
            requiredScreen = "movies"
        ),
        AIIntentDescriptor(
            intentId = "movies_trending_week",
            description = "Show trending movies this week",
            parameters = emptyList(),
            requiredScreen = "movies"
        ),
        AIIntentDescriptor(
            intentId = "movies_toggle_favorite",
            description = "Toggle favorite on the currently viewed movie",
            parameters = emptyList(),
            requiredScreen = "movie_detail"
        ),
        AIIntentDescriptor(
            intentId = "movies_filter",
            description = "Filter displayed movies by genre, title, year, rating, etc. Returns IDs of matching movies",
            parameters = listOf(
                AIIntentParameter("matchedIds", "Comma-separated list of matching movie IDs", "string", true),
                AIIntentParameter("filterLabel", "Short label describing the filter", "string", true)
            ),
            requiredScreen = "movies"
        ),
        AIIntentDescriptor(
            intentId = "movies_clear_filter",
            description = "Clear active filter, show all movies",
            parameters = emptyList(),
            requiredScreen = "movies"
        ),
        AIIntentDescriptor(
            intentId = "movies_navigate_detail",
            description = "Navigate to a specific movie's detail screen",
            parameters = listOf(
                AIIntentParameter("movieId", "The ID of the movie to navigate to", "int", true)
            ),
            requiredScreen = "movies"
        )
    )

    override fun resolve(resolvedIntent: ResolvedAIIntent): AIIntent? {
        return when (resolvedIntent.intentId) {
            "movies_trending_today" -> MoviesIntent.ChangeTimeWindow(TimeWindow.DAY)
            "movies_trending_week" -> MoviesIntent.ChangeTimeWindow(TimeWindow.WEEK)
            "movies_toggle_favorite" -> MovieDetailIntent.ToggleFavorite
            "movies_filter" -> {
                val ids = resolvedIntent.parameters["matchedIds"]
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: return null
                val label = resolvedIntent.parameters["filterLabel"] ?: "Filtered"
                MoviesIntent.ApplyFilter(ids, label)
            }

            "movies_clear_filter" -> MoviesIntent.ClearFilter
            "movies_navigate_detail" -> {
                val movieId = resolvedIntent.parameters["movieId"]?.toIntOrNull() ?: return null
                MoviesIntent.NavigateToDetail(movieId)
            }
            else -> null
        }
    }
}
