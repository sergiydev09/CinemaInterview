package com.cinema.movies.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.ai.domain.model.AIIntentDescriptor
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.movies.ui.feature.detail.MovieDetailIntent
import com.cinema.movies.ui.feature.list.MoviesIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesAIIntentHandler @Inject constructor() : AIIntentHandler() {

    override val featureId: String = "movies"

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
        )
    )

    override fun resolve(resolvedIntent: ResolvedAIIntent): AIIntent? {
        return when (resolvedIntent.intentId) {
            "movies_trending_today" -> MoviesIntent.ChangeTimeWindow(TimeWindow.DAY)
            "movies_trending_week" -> MoviesIntent.ChangeTimeWindow(TimeWindow.WEEK)
            "movies_toggle_favorite" -> MovieDetailIntent.ToggleFavorite
            else -> null
        }
    }
}
