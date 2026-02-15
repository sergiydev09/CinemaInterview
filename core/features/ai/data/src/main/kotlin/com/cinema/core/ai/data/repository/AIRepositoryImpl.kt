package com.cinema.core.ai.data.repository

import android.util.Log
import com.cinema.core.ai.data.network.OpenRouterApiService
import com.cinema.core.ai.data.network.model.ChatCompletionRequestDTO
import com.cinema.core.ai.data.network.model.ChatMessageDTO
import com.cinema.core.ai.data.network.model.ResolvedIntentDTO
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import com.cinema.core.ai.domain.repository.AIRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepositoryImpl @Inject constructor(
    private val apiService: OpenRouterApiService,
    private val json: Json
) : AIRepository {

    private val lenientJson = Json(json) {
        isLenient = true
    }

    override suspend fun resolveIntent(
        text: String,
        availableIntents: String,
        currentScreen: String?,
        screenContext: String?,
        isFollowUp: Boolean
    ): ResolvedAIIntent {
        Log.d(TAG, "Voice input: \"$text\" (screen: $currentScreen, followUp: $isFollowUp)")

        val systemPrompt = buildSystemPrompt(availableIntents, currentScreen, screenContext, isFollowUp)
        val request = ChatCompletionRequestDTO(
            model = MODEL,
            messages = listOf(
                ChatMessageDTO(role = "system", content = systemPrompt),
                ChatMessageDTO(role = "user", content = text)
            )
        )

        val response = apiService.createChatCompletion(request)
        val content = response.choices.first().message.content
        Log.d(TAG, "AI response: $content")

        val cleanContent = content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val dto = lenientJson.decodeFromString<ResolvedIntentDTO>(cleanContent)

        val resolved = ResolvedAIIntent(
            intentId = dto.intentId,
            parameters = dto.parameters,
            navigationTarget = dto.navigationTarget,
            confidence = dto.confidence,
            fallbackMessage = dto.fallbackMessage,
            needsFollowUp = dto.needsFollowUp,
            nextIntents = dto.nextIntents.map { chained ->
                ResolvedAIIntent(
                    intentId = chained.intentId,
                    parameters = chained.parameters,
                    navigationTarget = chained.navigationTarget
                )
            }
        )
        Log.d(TAG, "Resolved intent: ${resolved.intentId} (confidence: ${resolved.confidence}, target: ${resolved.navigationTarget}, followUp: ${resolved.needsFollowUp})")
        return resolved
    }

    companion object {
        private const val TAG = "AIRepository"
        private const val MODEL = "stepfun/step-3.5-flash:free"
    }

    private fun buildSystemPrompt(
        availableIntents: String,
        currentScreen: String?,
        screenContext: String?,
        isFollowUp: Boolean
    ): String {
        return buildString {
            appendLine("You are an AI assistant for a Cinema app. Analyze the user's voice command and determine which action they want to perform.")
            appendLine()
            appendLine("Available actions:")
            appendLine(availableIntents)
            appendLine()
            if (currentScreen != null) {
                appendLine("The user is currently on the \"$currentScreen\" screen.")
                appendLine()
            }
            if (isFollowUp) {
                appendLine("IMPORTANT: A preparatory action (navigation or time window change) has already been executed for this command.")
                appendLine("Determine what the NEXT action is from the user's original command.")
                appendLine("If the user's command still requires another preparatory step (e.g., changing time window to week after navigating), return that action with \"needsFollowUp\": true.")
                appendLine("If the next action is a final action (e.g., filtering or navigating to detail), return it with \"needsFollowUp\": false.")
                appendLine("If no additional action is needed, return intentId \"unknown\".")
                appendLine()
            }
            if (screenContext != null) {
                appendLine("Items currently displayed on screen:")
                appendLine(screenContext)
                appendLine()
                appendLine("IMPORTANT: The first line above tells you the current time window (today/this week). If the user asks for a DIFFERENT time window (e.g., says \"today\" but screen shows \"this week\", or vice versa), you MUST change the time window BEFORE filtering. This is a preparatory step.")
                appendLine("When filtering, return matching item IDs in the \"matchedIds\" parameter as comma-separated list.")
                appendLine("When navigating to a specific item's detail, return its ID in the appropriate parameter.")
                appendLine()
            }
            if (!isFollowUp) {
                appendLine("MULTI-STEP COMMANDS: If the user's command requires multiple preparatory steps before the final action (e.g., navigating AND changing time window before filtering), return the FIRST preparatory action as the main intent and ALL SUBSEQUENT preparatory actions in the \"nextIntents\" array, then set \"needsFollowUp\" to true.")
                appendLine("Example: \"show me romantic movies this week\" from home → intentId=\"navigate_movies\", navigationTarget=\"movies\", nextIntents=[{\"intentId\":\"movies_trending_week\"}], needsFollowUp=true")
                appendLine("Example: \"show me trending movies this week\" from home → intentId=\"navigate_movies\", navigationTarget=\"movies\", nextIntents=[{\"intentId\":\"movies_trending_week\"}], needsFollowUp=false (no filter needed)")
                appendLine("If only ONE preparatory step is needed, leave \"nextIntents\" empty.")
                appendLine("If the command can be fully resolved with a single action, set \"needsFollowUp\" to false and leave \"nextIntents\" empty.")
                appendLine()
            }
            appendLine("Respond with a JSON object containing:")
            appendLine("- \"intentId\": the ID of the matching action")
            appendLine("- \"parameters\": a map of parameter names to string values (empty object if none). All values MUST be quoted strings.")
            appendLine("- \"navigationTarget\": the screen to navigate to if the action requires a different screen (null if current screen is correct)")
            appendLine("- \"confidence\": a float from 0 to 1 indicating how confident you are")
            appendLine("- \"fallbackMessage\": a helpful message if no action matches (null if an action matches)")
            appendLine("- \"needsFollowUp\": true if a context-dependent action (like filtering) still remains after executing all preparatory steps, false otherwise")
            appendLine("- \"nextIntents\": array of additional preparatory actions to execute after the main intent (each with intentId, parameters, navigationTarget). Empty array if none.")
            appendLine()
            appendLine("If no action matches the user's request, set intentId to \"unknown\" and provide a fallbackMessage.")
            appendLine()
            appendLine("IMPORTANT: Respond ONLY with the JSON object. No markdown, no explanation, no code blocks.")
        }
    }
}
