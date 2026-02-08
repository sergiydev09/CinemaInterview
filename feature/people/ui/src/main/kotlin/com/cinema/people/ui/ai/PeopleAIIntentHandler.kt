package com.cinema.people.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.ai.domain.model.AIIntentDescriptor
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.people.ui.feature.detail.PersonDetailIntent
import com.cinema.people.ui.feature.list.PeopleIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleAIIntentHandler @Inject constructor() : AIIntentHandler() {

    override val featureId: String = "people"

    override fun getDescriptors(): List<AIIntentDescriptor> = listOf(
        AIIntentDescriptor(
            intentId = "navigate_people",
            description = "Navigate to the people screen to browse trending people",
            parameters = emptyList(),
            requiredScreen = null
        ),
        AIIntentDescriptor(
            intentId = "people_trending_today",
            description = "Show trending people today",
            parameters = emptyList(),
            requiredScreen = "people"
        ),
        AIIntentDescriptor(
            intentId = "people_trending_week",
            description = "Show trending people this week",
            parameters = emptyList(),
            requiredScreen = "people"
        ),
        AIIntentDescriptor(
            intentId = "people_toggle_favorite",
            description = "Toggle favorite on the currently viewed person",
            parameters = emptyList(),
            requiredScreen = "person_detail"
        )
    )

    override fun resolve(resolvedIntent: ResolvedAIIntent): AIIntent? {
        return when (resolvedIntent.intentId) {
            "people_trending_today" -> PeopleIntent.ChangeTimeWindow(TimeWindow.DAY)
            "people_trending_week" -> PeopleIntent.ChangeTimeWindow(TimeWindow.WEEK)
            "people_toggle_favorite" -> PersonDetailIntent.ToggleFavorite
            else -> null
        }
    }
}
