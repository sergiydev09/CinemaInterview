package com.cinema.core.ai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.manager.AIManager
import com.cinema.core.ai.domain.manager.AIMode
import com.cinema.core.ai.domain.manager.AIState
import com.cinema.core.ai.ui.speech.SpeechRecognizerManager
import com.cinema.core.ai.ui.speech.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIAssistantViewModel @Inject constructor(
    private val aiManager: AIManager,
    private val speechRecognizerManager: SpeechRecognizerManager
) : ViewModel() {

    val aiState: StateFlow<AIState> = aiManager.aiState

    init {
        observeSpeechState()
    }

    private fun observeSpeechState() {
        viewModelScope.launch {
            speechRecognizerManager.state.filterNotNull().collect { state ->
                when (state) {
                    is SpeechState.PartialResult -> {
                        aiManager.updateInputText(state.text)
                    }

                    is SpeechState.Result -> {
                        aiManager.updateInputText(state.text)
                        aiManager.stopListening()
                        speechRecognizerManager.stopListening()
                        speechRecognizerManager.resetState()
                    }

                    is SpeechState.Error -> {
                        aiManager.stopListening()
                        speechRecognizerManager.stopListening()
                        speechRecognizerManager.resetState()
                    }

                    else -> {}
                }
            }
        }
    }

    fun onFabClick() {
        if (aiManager.aiState.value.mode != AIMode.INACTIVE) {
            aiManager.deactivateAI()
            speechRecognizerManager.stopListening()
            speechRecognizerManager.resetState()
        } else {
            aiManager.activateAI()
        }
    }

    fun onMicClick() {
        val mode = aiManager.aiState.value.mode
        if (mode == AIMode.LISTENING) {
            aiManager.stopListening()
            speechRecognizerManager.stopListening()
            speechRecognizerManager.resetState()
        }
        // READY → need permission check first (handled in composable)
    }

    fun onMicPermissionGranted() {
        aiManager.startListening()
        speechRecognizerManager.startListening()
    }

    fun onInputTextChanged(text: String) {
        aiManager.updateInputText(text)
    }

    fun onSubmit() {
        val text = aiManager.aiState.value.inputText.trim()
        if (text.isEmpty()) return
        speechRecognizerManager.stopListening()
        speechRecognizerManager.resetState()
        viewModelScope.launch {
            aiManager.processInput(text)
        }
    }

    fun setCurrentScreen(screenId: String) {
        aiManager.setCurrentScreen(screenId)
    }
}
