package com.cinema.login.domain.usecase

import com.cinema.login.domain.model.ValidationErrorType
import javax.inject.Inject

/**
 * Use case for validating login credentials.
 * Password requirements:
 * - Minimum 8 characters
 * - At least one uppercase letter
 * - At least one lowercase letter
 * - At least one digit
 * - At least one special character
 */
class ValidateCredentialsUseCase @Inject constructor() {

    companion object {
        private const val MIN_USERNAME_LENGTH = 4
        private const val MIN_PASSWORD_LENGTH = 8
    }

    private fun validateUsername(username: String): List<ValidationErrorType> {
        val errors = mutableListOf<ValidationErrorType>()

        if (username.length < MIN_USERNAME_LENGTH) {
            errors.add(ValidationErrorType.USERNAME_TOO_SHORT)
        }

        return errors
    }

    private fun validatePassword(password: String): List<ValidationErrorType> {
        val errors = mutableListOf<ValidationErrorType>()

        if (password.length < MIN_PASSWORD_LENGTH) {
            errors.add(ValidationErrorType.PASSWORD_TOO_SHORT)
        }

        if (!password.any { it.isUpperCase() }) {
            errors.add(ValidationErrorType.PASSWORD_NO_UPPERCASE)
        }

        if (!password.any { it.isLowerCase() }) {
            errors.add(ValidationErrorType.PASSWORD_NO_LOWERCASE)
        }

        if (!password.any { it.isDigit() }) {
            errors.add(ValidationErrorType.PASSWORD_NO_DIGIT)
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add(ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR)
        }

        return errors
    }

    operator fun invoke(username: String, password: String): List<ValidationErrorType> {
        return validateUsername(username) + validatePassword(password)
    }
}
