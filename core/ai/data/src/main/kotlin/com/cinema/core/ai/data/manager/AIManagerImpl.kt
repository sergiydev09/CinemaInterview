package com.cinema.core.ai.data.manager

import android.util.Log
import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.manager.AIManager
import com.cinema.core.ai.domain.manager.AIMode
import com.cinema.core.ai.domain.manager.AINavigator
import com.cinema.core.ai.domain.manager.AIState
import com.cinema.core.ai.domain.repository.AIRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIManagerImpl @Inject constructor(
    private val handlers: Set<@JvmSuppressWildcards AIIntentHandler>,
    private val aiRepository: AIRepository,
    private val aiNavigator: AINavigator,
    private val json: Json
) : AIManager {

    private val _aiState = MutableStateFlow(AIState())
    override val aiState: StateFlow<AIState> = _aiState.asStateFlow()

    private var currentScreen: String? = null

    override fun setCurrentScreen(screenId: String) {
        Log.d(TAG, "Current screen: $screenId")
        currentScreen = screenId
    }

    override fun activateAI() {
        Log.d(TAG, "AI activated - ready for input")
        _aiState.update { AIState(mode = AIMode.READY) }
    }

    override fun deactivateAI() {
        Log.d(TAG, "AI deactivated")
        _aiState.update { AIState() }
    }

    override fun startListening() {
        Log.d(TAG, "Started listening")
        _aiState.update { it.copy(mode = AIMode.LISTENING, statusMessage = "Listening...") }
    }

    override fun stopListening() {
        Log.d(TAG, "Stopped listening")
        _aiState.update { it.copy(mode = AIMode.READY, statusMessage = null) }
    }

    override fun updateInputText(text: String) {
        _aiState.update { it.copy(inputText = text) }
    }

    override suspend fun processInput(text: String) {
        Log.d(TAG, "Processing input: \"$text\"")
        _aiState.update {
            AIState(mode = AIMode.PROCESSING, inputText = text, statusMessage = "Processing...")
        }

        try {
            val availableIntents = buildIntentsJson()
            val resolved = aiRepository.resolveIntent(text, availableIntents, currentScreen)

            if (resolved.intentId == "unknown") {
                Log.d(TAG, "Unknown intent: ${resolved.fallbackMessage}")
                _aiState.update {
                    AIState(
                        mode = AIMode.ERROR,
                        inputText = text,
                        errorMessage = resolved.fallbackMessage ?: "Could not understand the command"
                    )
                }
                delay(5000)
                _aiState.update { AIState() }
                return
            }

            _aiState.update {
                AIState(mode = AIMode.EXECUTING, inputText = text, statusMessage = "Executing...")
            }

            val matchingHandler = handlers.firstNotNullOfOrNull { handler ->
                handler.resolve(resolved)?.let { intent -> handler to intent }
            }

            if (matchingHandler != null) {
                val (handler, intent) = matchingHandler
                Log.d(TAG, "Handler found: ${handler.featureId}, dispatching intent")
                handler.dispatch(intent)
            }

            val target = resolved.navigationTarget
            if (target != null && target != currentScreen) {
                Log.d(TAG, "Navigating to: $target")
                aiNavigator.navigateTo(target, resolved.parameters)
            }

            if (matchingHandler == null && target == null) {
                Log.d(TAG, "No handler found for intent: ${resolved.intentId}")
                _aiState.update {
                    AIState(
                        mode = AIMode.ERROR,
                        inputText = text,
                        errorMessage = "No handler found for this action"
                    )
                }
            }

            _aiState.update { AIState() }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing input", e)
            _aiState.update {
                AIState(
                    mode = AIMode.ERROR,
                    inputText = text,
                    errorMessage = e.message ?: "An error occurred"
                )
            }
            delay(5000)
            _aiState.update { AIState() }
        }
    }

    companion object {
        private const val TAG = "AIManager"
    }

    private fun buildIntentsJson(): String {
        val intentsArray = buildJsonArray {
            for (handler in handlers) {
                for (descriptor in handler.getDescriptors()) {
                    add(buildJsonObject {
                        put("intentId", descriptor.intentId)
                        put("description", descriptor.description)
                        put("requiredScreen", descriptor.requiredScreen)
                        putJsonArray("parameters") {
                            for (param in descriptor.parameters) {
                                add(buildJsonObject {
                                    put("name", param.name)
                                    put("description", param.description)
                                    put("type", param.type)
                                    put("required", param.required)
                                })
                            }
                        }
                    })
                }
            }
        }
        return json.encodeToString(intentsArray)
    }
}
