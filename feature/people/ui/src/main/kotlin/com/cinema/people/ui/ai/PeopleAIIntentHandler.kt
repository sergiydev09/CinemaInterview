package com.cinema.people.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.ai.domain.model.AIIntentDescriptor
import com.cinema.core.ai.domain.model.AIIntentParameter
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.people.domain.model.Person
import com.cinema.people.ui.feature.detail.PersonDetailIntent
import com.cinema.people.ui.feature.list.PeopleIntent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleAIIntentHandler @Inject constructor() : AIIntentHandler() {

    override val featureId: String = "people"

    @Volatile
    private var currentPeople: List<Person> = emptyList()

    @Volatile
    private var currentTimeWindow: TimeWindow = TimeWindow.DAY

    fun setScreenData(people: List<Person>, timeWindow: TimeWindow) {
        currentPeople = people
        currentTimeWindow = timeWindow
        notifyScreenDataLoadCompleted()
    }

    override fun getScreenContext(): String? {
        if (currentPeople.isEmpty()) return null
        return buildString {
            val windowLabel = if (currentTimeWindow == TimeWindow.DAY) "today" else "this week"
            appendLine("Currently showing: trending people $windowLabel")
            currentPeople.forEach { person ->
                val knownFor = person.knownFor.take(3).joinToString(",") { it.title }
                appendLine("id=${person.id} \"${person.name}\" dept=${person.knownForDepartment} knownFor=[$knownFor]")
            }
        }.trimEnd()
    }

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
        ),
        AIIntentDescriptor(
            intentId = "people_filter",
            description = "Filter displayed people by department, known-for titles, etc. Use world knowledge for age queries",
            parameters = listOf(
                AIIntentParameter("matchedIds", "Comma-separated list of matching person IDs", "string", true),
                AIIntentParameter("filterLabel", "Short label describing the filter", "string", true)
            ),
            requiredScreen = "people"
        ),
        AIIntentDescriptor(
            intentId = "people_clear_filter",
            description = "Clear active filter, show all people",
            parameters = emptyList(),
            requiredScreen = "people"
        ),
        AIIntentDescriptor(
            intentId = "people_navigate_detail",
            description = "Navigate to a specific person's detail screen",
            parameters = listOf(
                AIIntentParameter("personId", "The ID of the person to navigate to", "int", true)
            ),
            requiredScreen = "people"
        )
    )

    override fun resolve(resolvedIntent: ResolvedAIIntent): AIIntent? {
        return when (resolvedIntent.intentId) {
            "people_trending_today" -> PeopleIntent.ChangeTimeWindow(TimeWindow.DAY)
            "people_trending_week" -> PeopleIntent.ChangeTimeWindow(TimeWindow.WEEK)
            "people_toggle_favorite" -> PersonDetailIntent.ToggleFavorite
            "people_filter" -> {
                val ids = resolvedIntent.parameters["matchedIds"]
                    ?.split(",")
                    ?.mapNotNull { it.trim().toIntOrNull() }
                    ?: return null
                val label = resolvedIntent.parameters["filterLabel"] ?: "Filtered"
                PeopleIntent.ApplyFilter(ids, label)
            }

            "people_clear_filter" -> PeopleIntent.ClearFilter
            "people_navigate_detail" -> {
                val personId = resolvedIntent.parameters["personId"]?.toIntOrNull() ?: return null
                PeopleIntent.NavigateToDetail(personId)
            }
            else -> null
        }
    }
}
