package com.cinema.login.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.repository.UserPreferencesRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetSavedUsernameUseCaseTest {

    private lateinit var repository: UserPreferencesRepository
    private lateinit var useCase: GetSavedUsernameUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetSavedUsernameUseCase(repository)
    }

    @Test
    fun `invoke emits Loading then Success with saved username`() = runTest {
        coEvery { repository.getSavedUsername() } returns "savedUser"

        useCase().test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals("savedUser", (success as Result.Success).data)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading then Success with null when no username saved`() = runTest {
        coEvery { repository.getSavedUsername() } returns null

        useCase().test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertNull((success as Result.Success).data)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading then Error on exception`() = runTest {
        coEvery { repository.getSavedUsername() } throws RuntimeException("Storage error")

        useCase().test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Storage error", (error as Result.Error).message)

            awaitComplete()
        }
    }
}
