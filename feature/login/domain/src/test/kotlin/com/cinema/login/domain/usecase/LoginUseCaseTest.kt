package com.cinema.login.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.model.Credentials
import com.cinema.login.domain.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var repository: LoginRepository
    private lateinit var useCase: LoginUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct credentials`() = runTest {
        val credentialsSlot = slot<Credentials>()
        coEvery { repository.login(capture(credentialsSlot)) } returns "token"

        useCase("user", "pass").test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        assertEquals("user", credentialsSlot.captured.username)
        assertEquals("pass", credentialsSlot.captured.password)
    }

    @Test
    fun `invoke returns Loading then Success`() = runTest {
        coEvery { repository.login(any()) } returns "token123"

        useCase("user", "pass").test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals("token123", (success as Result.Success).data)

            awaitComplete()
        }
    }

    @Test
    fun `invoke returns Loading then Error on exception`() = runTest {
        coEvery { repository.login(any()) } throws RuntimeException("Invalid credentials")

        useCase("user", "pass").test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Invalid credentials", (error as Result.Error).message)

            awaitComplete()
        }
    }
}
