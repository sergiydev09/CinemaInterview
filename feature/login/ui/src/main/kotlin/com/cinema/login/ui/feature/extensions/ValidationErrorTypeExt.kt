package com.cinema.login.ui.feature.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_NO_DIGIT
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_NO_LOWERCASE
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_NO_UPPERCASE
import com.cinema.login.domain.model.ValidationErrorType.PASSWORD_TOO_SHORT
import com.cinema.login.domain.model.ValidationErrorType.USERNAME_TOO_SHORT
import com.cinema.login.ui.R

@Composable
fun ValidationErrorType.toErrorString(): String = when (this) {
    USERNAME_TOO_SHORT -> stringResource(R.string.error_username_too_short)
    PASSWORD_TOO_SHORT -> stringResource(R.string.error_password_too_short)
    PASSWORD_NO_UPPERCASE -> stringResource(R.string.error_password_no_uppercase)
    PASSWORD_NO_LOWERCASE -> stringResource(R.string.error_password_no_lowercase)
    PASSWORD_NO_DIGIT -> stringResource(R.string.error_password_no_digit)
    PASSWORD_NO_SPECIAL_CHAR -> stringResource(R.string.error_password_no_special_char)
}
