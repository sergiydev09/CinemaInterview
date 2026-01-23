package com.cinema.people.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.repository.PeopleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetPersonDetailUseCaseTest {

    private lateinit var repository: PeopleRepository
    private lateinit var useCase: GetPersonDetailUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetPersonDetailUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct personId`() = runTest {
        val detail = createPersonDetail(123)
        coEvery { repository.getPersonDetail(123) } returns detail

        useCase(123).test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        coVerify { repository.getPersonDetail(123) }
    }

    @Test
    fun `invoke emits Loading then Success with person detail`() = runTest {
        val detail = createPersonDetail(123)
        coEvery { repository.getPersonDetail(123) } returns detail

        useCase(123).test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(123, (success as Result.Success).data.id)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading then Error on exception`() = runTest {
        coEvery { repository.getPersonDetail(123) } throws RuntimeException("Not found")

        useCase(123).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Not found", (error as Result.Error).message)

            awaitComplete()
        }
    }

    private fun createPersonDetail(id: Int) = PersonDetail(
        id = id,
        name = "John Doe",
        biography = "Biography",
        birthday = "1980-01-15",
        deathday = null,
        placeOfBirth = "Los Angeles",
        profileUrl = "https://image.tmdb.org/t/p/w185/profile.jpg",
        popularity = 100.0,
        knownForDepartment = "Acting",
        homepage = null,
        alsoKnownAs = emptyList()
    )
}
