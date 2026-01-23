package com.cinema.login.ui.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.domain.usecase.ClearUsernameUseCase
import com.cinema.login.domain.usecase.GetSavedUsernameUseCase
import com.cinema.login.domain.usecase.LoginUseCase
import com.cinema.login.domain.usecase.SaveUsernameUseCase
import com.cinema.login.domain.usecase.ValidateCredentialsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the login screen.
 */
data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val rememberUsername: Boolean = false,
    val isLoading: Boolean = false,
    val usernameErrors: List<ValidationErrorType> = emptyList(),
    val passwordErrors: List<ValidationErrorType> = emptyList(),
    val showInvalidCredentialsError: Boolean = false
) {
    val isLoginButtonEnabled: Boolean
        get() = username.isNotBlank() && password.isNotBlank() && !isLoading
}

/**
 * One-time events from the login screen.
 */
sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
}

/**
 * ViewModel for the login screen.
 * Handles user input validation and login logic.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateCredentialsUseCase: ValidateCredentialsUseCase,
    private val getSavedUsernameUseCase: GetSavedUsernameUseCase,
    private val saveUsernameUseCase: SaveUsernameUseCase,
    private val clearUsernameUseCase: ClearUsernameUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        loadSavedUsername()
    }

    private fun loadSavedUsername() {
        viewModelScope.launch {
            getSavedUsernameUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let { savedUsername ->
                            _uiState.update {
                                it.copy(
                                    username = savedUsername,
                                    rememberUsername = true
                                )
                            }
                        }
                    }
                    is Result.Error -> { /* Ignore errors loading saved username */ }
                    is Result.Loading -> { /* No loading state needed for local operation */ }
                }
            }
        }
    }

    /**
     * Called when the username text changes.
     */
    fun onUsernameChanged(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameErrors = emptyList(),
                passwordErrors = emptyList(),
                showInvalidCredentialsError = false
            )
        }
    }

    /**
     * Called when the password text changes.
     */
    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                usernameErrors = emptyList(),
                passwordErrors = emptyList(),
                showInvalidCredentialsError = false
            )
        }
    }

    /**
     * Called when the remember username checkbox changes.
     */
    fun onRememberUsernameChanged(remember: Boolean) {
        _uiState.update { it.copy(rememberUsername = remember) }
    }

    /**
     * Called when the login button is clicked.
     */
    fun onLoginClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        // Validate credentials first
        val validationErrors = validateCredentialsUseCase(state.username, state.password)
        if (validationErrors.isNotEmpty()) {
            handleValidationErrors(validationErrors)
            return
        }

        // If validation passes, attempt login
        viewModelScope.launch {
            loginUseCase(state.username, state.password).collect { result ->
                when (result) {
                    is Result.Success -> {
                        handleLoginSuccess(result.data)
                    }

                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showInvalidCredentialsError = true
                            )
                        }
                    }

                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, showInvalidCredentialsError = false) }
                    }
                }
            }
        }
    }

    private suspend fun handleLoginSuccess(token: String) {
        val state = _uiState.value

        // Save or clear username based on remember preference (ignore errors for local operations)
        if (state.rememberUsername) {
            saveUsernameUseCase(state.username).collect { /* Fire and forget */ }
        } else {
            clearUsernameUseCase().collect { /* Fire and forget */ }
        }

        sessionManager.startSession(token)
        _uiState.update { it.copy(isLoading = false) }
        _events.emit(LoginEvent.NavigateToHome)
    }

    private fun handleValidationErrors(errors: List<ValidationErrorType>) {
        val usernameErrors = errors.filter { it == ValidationErrorType.USERNAME_TOO_SHORT }
        val passwordErrors = errors.filter { it != ValidationErrorType.USERNAME_TOO_SHORT }

        _uiState.update {
            it.copy(
                usernameErrors = usernameErrors,
                passwordErrors = passwordErrors
            )
        }
    }
}
