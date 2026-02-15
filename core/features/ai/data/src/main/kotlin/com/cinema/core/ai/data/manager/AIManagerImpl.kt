package com.cinema.core.ai.data.manager

import android.util.Log
import com.cinema.core.ai.domain.handler.AIIntentHandler
import com.cinema.core.ai.domain.manager.AIManager
import com.cinema.core.ai.domain.manager.AIMode
import com.cinema.core.ai.domain.manager.AINavigator
import com.cinema.core.ai.domain.manager.AIState
import com.cinema.core.ai.domain.model.ResolvedAIIntent
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
            AIState(mode = AIMode.PROCESSING, inputText = text, statusMessage = "Understanding...")
        }

        try {
            val availableIntents = buildIntentsJson()
            var activeScreen = currentScreen
            val screenContext = findHandlerForScreen(activeScreen)?.getScreenContext()

            // Stage 1: Plan — determine what the user wants
            val resolved = aiRepository.resolveIntent(text, availableIntents, activeScreen, screenContext)

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

            if (!resolved.needsFollowUp) {
                // Single-step: execute and finish
                _aiState.update {
                    AIState(mode = AIMode.EXECUTING, inputText = text, statusMessage = "Executing...")
                }
                val executed = executeIntent(resolved)
                if (!executed) {
                    Log.d(TAG, "No handler found for intent: ${resolved.intentId}")
                    _aiState.update {
                        AIState(
                            mode = AIMode.ERROR,
                            inputText = text,
                            errorMessage = "No handler found for this action"
                        )
                    }
                    delay(3000)
                }
                _aiState.update { AIState() }
                return
            }

            // Multi-step: build full chain [main intent + nextIntents]
            val chain = buildList {
                add(resolved)
                addAll(resolved.nextIntents)
            }
            Log.d(TAG, "Intent chain: ${chain.map { it.intentId }}")

            // Execute each preparatory step, waiting for data after each
            val totalSteps = chain.size + 1 // chain steps + follow-up resolution

            for ((index, step) in chain.withIndex()) {
                val stepNum = index + 1
                Log.d(TAG, "Executing chain step $stepNum/$totalSteps: ${step.intentId}")
                _aiState.update {
                    AIState(
                        mode = AIMode.EXECUTING,
                        inputText = text,
                        statusMessage = "Step $stepNum of $totalSteps..."
                    )
                }

                val targetScreen = step.navigationTarget ?: activeScreen
                val targetHandler = findHandlerForScreen(targetScreen)

                targetHandler?.prepareForScreenDataUpdate()
                executeIntent(step)

                Log.d(TAG, "Awaiting screen data for $targetScreen...")
                targetHandler?.awaitScreenDataUpdate()
                Log.d(TAG, "Screen data ready for $targetScreen")

                activeScreen = targetScreen
            }

            // Follow-up: resolve final intent with fresh context
            val handler = findHandlerForScreen(activeScreen)
            val newContext = handler?.getScreenContext()
            if (newContext != null) {
                _aiState.update {
                    AIState(
                        mode = AIMode.PROCESSING,
                        inputText = text,
                        statusMessage = "Step $totalSteps of $totalSteps..."
                    )
                }

                val finalResolved = aiRepository.resolveIntent(
                    text, availableIntents, activeScreen, newContext, isFollowUp = true
                )

                if (finalResolved.intentId != "unknown") {
                    _aiState.update {
                        AIState(mode = AIMode.EXECUTING, inputText = text, statusMessage = "Executing...")
                    }
                    Log.d(TAG, "Final intent: ${finalResolved.intentId}")
                    executeIntent(finalResolved)
                } else {
                    Log.d(TAG, "No follow-up action needed")
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

    private fun executeIntent(resolved: ResolvedAIIntent): Boolean {
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

        return matchingHandler != null || (target != null && target != currentScreen)
    }

    private fun findHandlerForScreen(screen: String?): AIIntentHandler? {
        if (screen == null) return null
        return handlers.firstOrNull { h ->
            h.getDescriptors().any { it.requiredScreen == screen }
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
