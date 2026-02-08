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

    override suspend fun resolveIntent(
        text: String,
        availableIntents: String,
        currentScreen: String?
    ): ResolvedAIIntent {
        Log.d(TAG, "Voice input: \"$text\" (screen: $currentScreen)")

        val systemPrompt = buildSystemPrompt(availableIntents, currentScreen)
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

        val dto = json.decodeFromString<ResolvedIntentDTO>(content)

        val resolved = ResolvedAIIntent(
            intentId = dto.intentId,
            parameters = dto.parameters,
            navigationTarget = dto.navigationTarget,
            confidence = dto.confidence,
            fallbackMessage = dto.fallbackMessage
        )
        Log.d(TAG, "Resolved intent: ${resolved.intentId} (confidence: ${resolved.confidence}, target: ${resolved.navigationTarget})")
        return resolved
    }

    companion object {
        private const val TAG = "AIRepository"
        private const val MODEL = "stepfun/step-3.5-flash:free"
    }

    private fun buildSystemPrompt(availableIntents: String, currentScreen: String?): String {
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
            appendLine("Respond with a JSON object containing:")
            appendLine("- \"intentId\": the ID of the matching action")
            appendLine("- \"parameters\": a map of parameter names to values (empty object if none)")
            appendLine("- \"navigationTarget\": the screen to navigate to if the action requires a different screen (null if current screen is correct)")
            appendLine("- \"confidence\": a float from 0 to 1 indicating how confident you are")
            appendLine("- \"fallbackMessage\": a helpful message if no action matches (null if an action matches)")
            appendLine()
            appendLine("If no action matches the user's request, set intentId to \"unknown\" and provide a fallbackMessage.")
            appendLine()
            appendLine("IMPORTANT: Respond ONLY with the JSON object. No markdown, no explanation, no code blocks.")
        }
    }
}
