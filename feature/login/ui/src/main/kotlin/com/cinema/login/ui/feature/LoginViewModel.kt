package com.cinema.login.ui.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.domain.model.ValidationErrorType.USERNAME_TOO_SHORT
import com.cinema.login.domain.repository.UserPreferencesRepository
import com.cinema.login.domain.usecase.LoginUseCase
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

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
}

sealed interface LoginIntent {
    data class UsernameChanged(val username: String) : LoginIntent
    data class PasswordChanged(val password: String) : LoginIntent
    data class RememberUsernameChanged(val remember: Boolean) : LoginIntent
    data object LoginClicked : LoginIntent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val validateCredentialsUseCase: ValidateCredentialsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    init {
        loadSavedUsername()
    }

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UsernameChanged -> onUsernameChanged(intent.username)
            is LoginIntent.PasswordChanged -> onPasswordChanged(intent.password)
            is LoginIntent.RememberUsernameChanged -> onRememberUsernameChanged(intent.remember)
            is LoginIntent.LoginClicked -> onLoginClicked()
        }
    }

    private fun loadSavedUsername() {
        viewModelScope.launch {
            userPreferencesRepository.getSavedUsername()?.let { savedUsername ->
                _uiState.update {
                    it.copy(
                        username = savedUsername,
                        rememberUsername = true
                    )
                }
            }
        }
    }

    private fun onUsernameChanged(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameErrors = emptyList(),
                passwordErrors = emptyList(),
                showInvalidCredentialsError = false
            )
        }
    }

    private fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                usernameErrors = emptyList(),
                passwordErrors = emptyList(),
                showInvalidCredentialsError = false
            )
        }
    }

    private fun onRememberUsernameChanged(remember: Boolean) {
        _uiState.update { it.copy(rememberUsername = remember) }
    }

    private fun onLoginClicked() {
        val state = _uiState.value
        if (state.isLoading) return

        val validationErrors = validateCredentialsUseCase(state.username, state.password)
        if (validationErrors.isNotEmpty()) {
            handleValidationErrors(validationErrors)
            return
        }

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

        if (state.rememberUsername) {
            userPreferencesRepository.saveUsername(state.username)
        } else {
            userPreferencesRepository.clearUsername()
        }

        sessionManager.startSession(token)
        _uiState.update { it.copy(isLoading = false) }
        _events.emit(LoginEvent.NavigateToHome)
    }

    private fun handleValidationErrors(errors: List<ValidationErrorType>) {
        val usernameErrors = errors.filter { it == USERNAME_TOO_SHORT }
        val passwordErrors = errors.filter { it != USERNAME_TOO_SHORT }

        _uiState.update {
            it.copy(
                usernameErrors = usernameErrors,
                passwordErrors = passwordErrors
            )
        }
    }
}
