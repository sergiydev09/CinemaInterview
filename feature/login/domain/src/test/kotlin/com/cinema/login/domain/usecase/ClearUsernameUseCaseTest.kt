package com.cinema.login.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClearUsernameUseCaseTest {

    private lateinit var repository: UserPreferencesRepository
    private lateinit var useCase: ClearUsernameUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ClearUsernameUseCase(repository)
    }

    @Test
    fun `invoke emits Loading then Success`() = runTest {
        coEvery { repository.clearUsername() } just runs

        useCase().test {
            assertTrue(awaitItem() is Result.Loading)
            assertTrue(awaitItem() is Result.Success)
            awaitComplete()
        }

        coVerify { repository.clearUsername() }
    }

    @Test
    fun `invoke emits Loading then Error on exception`() = runTest {
        coEvery { repository.clearUsername() } throws RuntimeException("Storage error")

        useCase().test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Storage error", (error as Result.Error).message)

            awaitComplete()
        }
    }
}
