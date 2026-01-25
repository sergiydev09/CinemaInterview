package com.cinema.core.favorites.domain.usecase

import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TogglePersonFavoriteUseCaseTest {

    private val repository: FavoritesRepository = mockk(relaxed = true)
    private val useCase = TogglePersonFavoriteUseCase(repository)

    private val person = FavoritePerson(
        id = 1,
        name = "Test Person",
        profileUrl = "https://example.com/profile.jpg"
    )

    @Test
    fun `invoke adds person when not favorite`() = runTest {
        coEvery { repository.isPersonFavorite(person.id) } returns flowOf(false)

        useCase(person)

        coVerify { repository.addFavoritePerson(person) }
        coVerify(exactly = 0) { repository.removeFavoritePerson(any()) }
    }

    @Test
    fun `invoke removes person when already favorite`() = runTest {
        coEvery { repository.isPersonFavorite(person.id) } returns flowOf(true)

        useCase(person)

        coVerify { repository.removeFavoritePerson(person.id) }
        coVerify(exactly = 0) { repository.addFavoritePerson(any()) }
    }
}
