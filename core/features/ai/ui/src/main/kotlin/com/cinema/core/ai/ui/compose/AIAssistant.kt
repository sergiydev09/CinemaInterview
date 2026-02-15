package com.cinema.core.ai.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cinema.core.ai.domain.manager.AIMode
import com.cinema.core.ai.ui.viewmodel.AIAssistantViewModel

@Composable
fun AIAssistant(
    modifier: Modifier = Modifier,
    viewModel: AIAssistantViewModel = hiltViewModel(),
    content: @Composable (
        aiViewModel: AIAssistantViewModel,
        aiFab: @Composable () -> Unit,
        aiInputBar: @Composable () -> Unit
    ) -> Unit
) {
    val aiState by viewModel.aiState.collectAsState()
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        focusManager.clearFocus()
                    }
                }
            }
    ) {
        content(
            viewModel,
            {
                AIFab(
                    aiMode = aiState.mode,
                    onClick = viewModel::onFabClick
                )
            },
            {
                AIInputBar(
                    aiState = aiState,
                    onTextChanged = viewModel::onInputTextChanged,
                    onMicClick = viewModel::onMicClick,
                    onMicPermissionGranted = viewModel::onMicPermissionGranted,
                    onSubmit = viewModel::onSubmit
                )
            }
        )

        if (aiState.mode != AIMode.INACTIVE) {
            AnimatedAIBorder()
        }
    }
}
