package com.cinema.movies.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
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

class GetTrendingMoviesUseCaseTest {

    private lateinit var moviesRepository: MoviesRepository
    private lateinit var favoritesRepository: FavoritesRepository
    private lateinit var useCase: GetTrendingMoviesUseCase

    private val favoriteMoviesFlow = MutableStateFlow<Map<Int, FavoriteMovie>>(emptyMap())

    @Before
    fun setup() {
        moviesRepository = mockk()
        favoritesRepository = mockk()
        every { favoritesRepository.favoriteMovies } returns favoriteMoviesFlow
        useCase = GetTrendingMoviesUseCase(moviesRepository, favoritesRepository)
    }

    @Test
    fun `invoke calls repository with correct timeWindow`() = runTest {
        val movies = listOf(createMovie(1))
        coEvery { moviesRepository.getTrendingMovies("week") } returns movies

        useCase(TimeWindow.WEEK).test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { moviesRepository.getTrendingMovies("week") }
    }

    @Test
    fun `invoke uses default timeWindow when not specified`() = runTest {
        val movies = listOf(createMovie(1))
        coEvery { moviesRepository.getTrendingMovies("day") } returns movies

        useCase().test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { moviesRepository.getTrendingMovies("day") }
    }

    @Test
    fun `invoke emits Loading then Success with movies`() = runTest {
        val movies = listOf(createMovie(1), createMovie(2))
        coEvery { moviesRepository.getTrendingMovies("day") } returns movies

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
        coEvery { moviesRepository.getTrendingMovies("day") } throws RuntimeException("Network error")

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke marks movies as favorite when in favorites`() = runTest {
        val movies = listOf(createMovie(1), createMovie(2))
        coEvery { moviesRepository.getTrendingMovies("day") } returns movies

        favoriteMoviesFlow.value = mapOf(
            1 to FavoriteMovie(id = 1, title = "Movie 1", posterUrl = null, releaseDate = null)
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
        val movies = listOf(createMovie(1))
        coEvery { moviesRepository.getTrendingMovies("day") } returns movies

        useCase(TimeWindow.DAY).test {
            awaitItem() // Loading

            val firstResult = awaitItem() as Result.Success
            assertFalse(firstResult.data[0].isFavorite)

            favoriteMoviesFlow.value = mapOf(
                1 to FavoriteMovie(id = 1, title = "Movie 1", posterUrl = null, releaseDate = null)
            )

            val secondResult = awaitItem() as Result.Success
            assertTrue(secondResult.data[0].isFavorite)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0
    )
}
