package com.cinema.login.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.login.ui.feature.views.LoginContent

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onUsernameChanged = { viewModel.handleIntent(LoginIntent.UsernameChanged(it)) },
        onPasswordChanged = { viewModel.handleIntent(LoginIntent.PasswordChanged(it)) },
        onRememberUsernameChanged = { viewModel.handleIntent(LoginIntent.RememberUsernameChanged(it)) },
        onLoginClicked = { viewModel.handleIntent(LoginIntent.LoginClicked) }
    )
}
