package com.cinema.login.ui.feature.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.InlineError
import com.cinema.core.ui.compose.LoadingButton
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_NO_UPPERCASE
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_TOO_SHORT
import com.cinema.login.domain.model.ValidationErrorType.USERNAME_TOO_SHORT
import com.cinema.login.ui.R
import com.cinema.login.ui.feature.LoginUiState
import com.cinema.login.ui.feature.extensions.toErrorString

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRememberUsernameChanged: (Boolean) -> Unit,
    onLoginClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.ic_movie_logo),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.login_title),
            style = typography.headlineMedium,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.login_subtitle),
            style = typography.bodyMedium,
            color = colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(R.string.hint_username)) },
            singleLine = true,
            isError = uiState.usernameErrors.isNotEmpty(),
            supportingText = {
                uiState.usernameErrors.firstOrNull()?.let { error ->
                    Text(error.toErrorString())
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.secondary,
                focusedLabelColor = colorScheme.secondary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(R.string.hint_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.passwordErrors.isNotEmpty(),
            supportingText = {
                uiState.passwordErrors.firstOrNull()?.let { error ->
                    Text(error.toErrorString())
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLoginClicked()
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.secondary,
                focusedLabelColor = colorScheme.secondary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.rememberUsername,
                onCheckedChange = onRememberUsernameChanged,
                colors = CheckboxDefaults.colors(
                    checkedColor = colorScheme.secondary
                )
            )
            Text(
                text = stringResource(R.string.remember_username),
                style = typography.bodyMedium,
                color = colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.showInvalidCredentialsError) {
            InlineError(
                message = stringResource(R.string.error_invalid_credentials),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        LoadingButton(
            text = stringResource(R.string.login_button),
            onClick = onLoginClicked,
            enabled = uiState.isLoginButtonEnabled,
            isLoading = uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentPreview() {
    CinemaTheme {
        Surface {
            LoginContent(
                uiState = LoginUiState(),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onRememberUsernameChanged = {},
                onLoginClicked = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentFilledPreview() {
    CinemaTheme {
        Surface {
            LoginContent(
                uiState = LoginUiState(
                    username = "testuser",
                    password = "Password123!",
                    rememberUsername = true
                ),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onRememberUsernameChanged = {},
                onLoginClicked = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentLoadingPreview() {
    CinemaTheme {
        Surface {
            LoginContent(
                uiState = LoginUiState(
                    username = "testuser",
                    password = "Password123!",
                    isLoading = true
                ),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onRememberUsernameChanged = {},
                onLoginClicked = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentWithErrorsPreview() {
    CinemaTheme {
        Surface {
            LoginContent(
                uiState = LoginUiState(
                    username = "ab",
                    password = "123",
                    usernameErrors = listOf(USERNAME_TOO_SHORT),
                    passwordErrors = listOf(
                        PASSWORD_TOO_SHORT,
                        PASSWORD_NO_UPPERCASE
                    )
                ),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onRememberUsernameChanged = {},
                onLoginClicked = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginContentInvalidCredentialsPreview() {
    CinemaTheme {
        Surface {
            LoginContent(
                uiState = LoginUiState(
                    username = "testuser",
                    password = "Password123!",
                    showInvalidCredentialsError = true
                ),
                onUsernameChanged = {},
                onPasswordChanged = {},
                onRememberUsernameChanged = {},
                onLoginClicked = {}
            )
        }
    }
}
