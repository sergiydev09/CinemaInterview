package com.cinema.people.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTrendingPeopleUseCaseTest {

    private lateinit var peopleRepository: PeopleRepository
    private lateinit var useCase: GetTrendingPeopleUseCase

    @Before
    fun setup() {
        peopleRepository = mockk()
        useCase = GetTrendingPeopleUseCase(peopleRepository)
    }

    @Test
    fun `invoke calls repository with correct timeWindow`() = runTest {
        val people = listOf(createPerson(1))
        every { peopleRepository.getTrendingPeople("week") } returns flowOf(people)

        useCase(TimeWindow.WEEK).test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        verify { peopleRepository.getTrendingPeople("week") }
    }

    @Test
    fun `invoke uses default timeWindow when not specified`() = runTest {
        val people = listOf(createPerson(1))
        every { peopleRepository.getTrendingPeople("day") } returns flowOf(people)

        useCase().test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        verify { peopleRepository.getTrendingPeople("day") }
    }

    @Test
    fun `invoke emits Loading then Success with people`() = runTest {
        val people = listOf(createPerson(1), createPerson(2))
        every { peopleRepository.getTrendingPeople("day") } returns flowOf(people)

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(2, (success as Result.Success).data.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke emits Loading then Error on exception`() = runTest {
        every { peopleRepository.getTrendingPeople("day") } returns flow { throw RuntimeException("Network error") }

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPerson(id: Int) = Person(
        id = id,
        name = "Person $id",
        profileUrl = "https://image.tmdb.org/t/p/w185/profile.jpg",
        popularity = 50.0,
        knownForDepartment = "Acting",
        knownFor = emptyList()
    )
}
