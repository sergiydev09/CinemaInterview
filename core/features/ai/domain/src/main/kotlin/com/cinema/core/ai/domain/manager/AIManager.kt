package com.cinema.core.ai.domain.manager

import kotlinx.coroutines.flow.StateFlow

enum class AIMode { INACTIVE, READY, LISTENING, PROCESSING, EXECUTING, ERROR }

data class AIState(
    val mode: AIMode = AIMode.INACTIVE,
    val inputText: String = "",
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

interface AIManager {
    val aiState: StateFlow<AIState>
    fun setCurrentScreen(screenId: String)
    fun activateAI()
    fun deactivateAI()
    fun startListening()
    fun stopListening()
    fun updateInputText(text: String)
    suspend fun processInput(text: String)
}
