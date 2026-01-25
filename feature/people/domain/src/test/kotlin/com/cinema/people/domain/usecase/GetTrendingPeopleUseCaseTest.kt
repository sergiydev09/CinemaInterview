package com.cinema.people.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTrendingPeopleUseCaseTest {

    private lateinit var peopleRepository: PeopleRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: GetTrendingPeopleUseCase

    private val favoritePeopleFlow = MutableStateFlow<Map<Int, FavoritePerson>>(emptyMap())

    @Before
    fun setup() {
        peopleRepository = mockk()
        favoritesRepository = mockk()
        every { favoritesRepository.favoritePeople } returns favoritePeopleFlow
        useCase = GetTrendingPeopleUseCase(peopleRepository, favoritesRepository)
    }

    @Test
    fun `invoke calls repository with correct timeWindow`() = runTest {
        val people = listOf(createPerson(1))
        coEvery { peopleRepository.getTrendingPeople("week") } returns people

        useCase(TimeWindow.WEEK).test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { peopleRepository.getTrendingPeople("week") }
    }

    @Test
    fun `invoke uses default timeWindow when not specified`() = runTest {
        val people = listOf(createPerson(1))
        coEvery { peopleRepository.getTrendingPeople("day") } returns people

        useCase().test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { peopleRepository.getTrendingPeople("day") }
    }

    @Test
    fun `invoke emits Loading then Success with people`() = runTest {
        val people = listOf(createPerson(1), createPerson(2))
        coEvery { peopleRepository.getTrendingPeople("day") } returns people

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
        coEvery { peopleRepository.getTrendingPeople("day") } throws RuntimeException("Network error")

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke marks people as favorite when in favorites`() = runTest {
        val people = listOf(createPerson(1), createPerson(2))
        coEvery { peopleRepository.getTrendingPeople("day") } returns people

        favoritePeopleFlow.value = mapOf(
            1 to FavoritePerson(id = 1, name = "Person 1", profileUrl = null)
        )

        useCase(TimeWindow.DAY).test {
            awaitItem() // Loading

            val success = awaitItem()
            assertTrue(success is Result.Success)
            val data = (success as Result.Success).data
            assertTrue(data[0].isFavorite)
            assertFalse(data[1].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke updates favorite status when favorites change`() = runTest {
        val people = listOf(createPerson(1))
        coEvery { peopleRepository.getTrendingPeople("day") } returns people

        useCase(TimeWindow.DAY).test {
            awaitItem() // Loading

            val firstResult = awaitItem() as Result.Success
            assertFalse(firstResult.data[0].isFavorite)

            favoritePeopleFlow.value = mapOf(
                1 to FavoritePerson(id = 1, name = "Person 1", profileUrl = null)
            )

            val secondResult = awaitItem() as Result.Success
            assertTrue(secondResult.data[0].isFavorite)

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
