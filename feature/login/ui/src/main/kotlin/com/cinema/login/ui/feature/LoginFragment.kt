package com.cinema.login.ui.feature

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.cinema.core.ui.base.BaseFragment
import com.cinema.core.ui.extension.gone
import com.cinema.core.ui.extension.visible
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.ui.LoginNavigator
import com.cinema.login.ui.R
import com.cinema.login.ui.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment handling the login UI.
 * Displays username and password fields with validation feedback.
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    private var loginNavigator: LoginNavigator? = null
    private var isUpdatingFromState = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LoginNavigator) {
            loginNavigator = context
        }
    }

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBinding {
        return FragmentLoginBinding.inflate(inflater, container, false)
    }

    override fun setupViews() {
        setupTextInputListeners()
        setupRememberUsernameCheckbox()
        setupLoginButton()
    }

    override fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        updateUi(state)
                    }
                }

                launch {
                    viewModel.events.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun setupTextInputListeners() {
        binding.usernameEditText.doAfterTextChanged { text ->
            if (!isUpdatingFromState) {
                viewModel.onUsernameChanged(text?.toString() ?: "")
            }
        }

        binding.passwordEditText.doAfterTextChanged { text ->
            if (!isUpdatingFromState) {
                viewModel.onPasswordChanged(text?.toString() ?: "")
            }
        }
    }

    private fun setupRememberUsernameCheckbox() {
        binding.rememberUsernameCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (!isUpdatingFromState) {
                viewModel.onRememberUsernameChanged(isChecked)
            }
        }
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnButtonClickListener {
            viewModel.onLoginClicked()
        }
    }

    private fun updateUi(state: LoginUiState) {
        isUpdatingFromState = true

        // Sync username from state (for saved username)
        if (binding.usernameEditText.text.toString() != state.username) {
            binding.usernameEditText.setText(state.username)
            binding.usernameEditText.setSelection(state.username.length)
        }

        // Sync remember checkbox
        if (binding.rememberUsernameCheckbox.isChecked != state.rememberUsername) {
            binding.rememberUsernameCheckbox.isChecked = state.rememberUsername
        }

        isUpdatingFromState = false

        // Loading state
        when {
            state.isLoading -> binding.loginButton.setLoading()
            state.isLoginButtonEnabled -> binding.loginButton.setEnabled()
            else -> binding.loginButton.setDisabled()
        }

        // Username error
        binding.usernameInputLayout.error = state.usernameErrors
            .firstOrNull()
            ?.toErrorString()

        // Password error
        binding.passwordInputLayout.error = state.passwordErrors
            .firstOrNull()
            ?.toErrorString()

        // General error
        if (state.showInvalidCredentialsError) {
            binding.errorView.show(getString(R.string.error_invalid_credentials))
        } else {
            binding.errorView.hide()
        }
    }

    private fun ValidationErrorType.toErrorString(): String {
        return when (this) {
            ValidationErrorType.USERNAME_TOO_SHORT -> getString(R.string.error_username_too_short)
            ValidationErrorType.PASSWORD_TOO_SHORT -> getString(R.string.error_password_too_short)
            ValidationErrorType.PASSWORD_NO_UPPERCASE -> getString(R.string.error_password_no_uppercase)
            ValidationErrorType.PASSWORD_NO_LOWERCASE -> getString(R.string.error_password_no_lowercase)
            ValidationErrorType.PASSWORD_NO_DIGIT -> getString(R.string.error_password_no_digit)
            ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR -> getString(R.string.error_password_no_special_char)
        }
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.NavigateToHome -> {
                loginNavigator?.onLoginSuccess()
            }
        }
    }

    override fun onDetach() {
        super.onDetach()
        loginNavigator = null
    }
}
