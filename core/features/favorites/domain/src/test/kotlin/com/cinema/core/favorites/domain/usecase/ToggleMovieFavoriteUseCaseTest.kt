package com.cinema.core.favorites.domain.usecase

import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleMovieFavoriteUseCaseTest {

    private val repository: FavoritesRepository = mockk(relaxed = true)
    private val useCase = ToggleMovieFavoriteUseCase(repository)

    private val movie = FavoriteMovie(
        id = 1,
        title = "Test Movie",
        posterUrl = "https://example.com/poster.jpg",
        releaseDate = "2024-01-01"
    )

    @Test
    fun `invoke adds movie when not favorite`() = runTest {
        coEvery { repository.isMovieFavorite(movie.id) } returns flowOf(false)

        useCase(movie)

        coVerify { repository.addFavoriteMovie(movie) }
        coVerify(exactly = 0) { repository.removeFavoriteMovie(any()) }
    }

    @Test
    fun `invoke removes movie when already favorite`() = runTest {
        coEvery { repository.isMovieFavorite(movie.id) } returns flowOf(true)

        useCase(movie)

        coVerify { repository.removeFavoriteMovie(movie.id) }
        coVerify(exactly = 0) { repository.addFavoriteMovie(any()) }
    }
}
