package com.cinema.login.domain.usecase

import com.cinema.login.domain.model.ValidationErrorType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateCredentialsUseCaseTest {

    private lateinit var useCase: ValidateCredentialsUseCase

    @Before
    fun setup() {
        useCase = ValidateCredentialsUseCase()
    }

    @Test
    fun `valid credentials return empty error list`() {
        val errors = useCase("username", "Password1!")

        assertTrue(errors.isEmpty())
    }

    @Test
    fun `username too short returns USERNAME_TOO_SHORT error`() {
        val errors = useCase("abc", "Password1!")

        assertTrue(errors.contains(ValidationErrorType.USERNAME_TOO_SHORT))
    }

    @Test
    fun `username with minimum length is valid`() {
        val errors = useCase("user", "Password1!")

        assertTrue(errors.none { it == ValidationErrorType.USERNAME_TOO_SHORT })
    }

    @Test
    fun `password too short returns PASSWORD_TOO_SHORT error`() {
        val errors = useCase("username", "Pass1!")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_TOO_SHORT))
    }

    @Test
    fun `password with minimum length is valid`() {
        val errors = useCase("username", "Passwo1!")

        assertTrue(errors.none { it == ValidationErrorType.PASSWORD_TOO_SHORT })
    }

    @Test
    fun `password without uppercase returns PASSWORD_NO_UPPERCASE error`() {
        val errors = useCase("username", "password1!")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_UPPERCASE))
    }

    @Test
    fun `password without lowercase returns PASSWORD_NO_LOWERCASE error`() {
        val errors = useCase("username", "PASSWORD1!")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_LOWERCASE))
    }

    @Test
    fun `password without digit returns PASSWORD_NO_DIGIT error`() {
        val errors = useCase("username", "Password!!")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_DIGIT))
    }

    @Test
    fun `password without special char returns PASSWORD_NO_SPECIAL_CHAR error`() {
        val errors = useCase("username", "Password12")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR))
    }

    @Test
    fun `multiple errors are returned for invalid credentials`() {
        val errors = useCase("ab", "pass")

        assertTrue(errors.contains(ValidationErrorType.USERNAME_TOO_SHORT))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_TOO_SHORT))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_UPPERCASE))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_DIGIT))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR))
        assertEquals(5, errors.size)
    }

    @Test
    fun `empty username fails validation`() {
        val errors = useCase("", "Password1!")

        assertTrue(errors.contains(ValidationErrorType.USERNAME_TOO_SHORT))
    }

    @Test
    fun `empty password fails validation`() {
        val errors = useCase("username", "")

        assertTrue(errors.contains(ValidationErrorType.PASSWORD_TOO_SHORT))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_UPPERCASE))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_LOWERCASE))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_DIGIT))
        assertTrue(errors.contains(ValidationErrorType.PASSWORD_NO_SPECIAL_CHAR))
    }
}
