package com.cinema.login.domain.model

/**
 * Types of validation errors that can occur during login.
 */
enum class ValidationErrorType {
    USERNAME_TOO_SHORT,
    PASSWORD_TOO_SHORT,
    PASSWORD_NO_UPPERCASE,
    PASSWORD_NO_LOWERCASE,
    PASSWORD_NO_DIGIT,
    PASSWORD_NO_SPECIAL_CHAR
}
