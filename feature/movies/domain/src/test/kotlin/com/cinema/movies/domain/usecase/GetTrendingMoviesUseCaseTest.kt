package com.cinema.movies.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
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

class GetTrendingMoviesUseCaseTest {

    private lateinit var moviesRepository: MoviesRepository
    private lateinit var useCase: GetTrendingMoviesUseCase

    @Before
    fun setup() {
        moviesRepository = mockk()
        useCase = GetTrendingMoviesUseCase(moviesRepository)
    }

    @Test
    fun `invoke calls repository with correct timeWindow`() = runTest {
        val movies = listOf(createMovie(1))
        every { moviesRepository.getTrendingMovies("week") } returns flowOf(movies)

        useCase(TimeWindow.WEEK).test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        verify { moviesRepository.getTrendingMovies("week") }
    }

    @Test
    fun `invoke uses default timeWindow when not specified`() = runTest {
        val movies = listOf(createMovie(1))
        every { moviesRepository.getTrendingMovies("day") } returns flowOf(movies)

        useCase().test {
            awaitItem() // Loading
            awaitItem() // Success
            cancelAndIgnoreRemainingEvents()
        }

        verify { moviesRepository.getTrendingMovies("day") }
    }

    @Test
    fun `invoke emits Loading then Success with movies`() = runTest {
        val movies = listOf(createMovie(1), createMovie(2))
        every { moviesRepository.getTrendingMovies("day") } returns flowOf(movies)

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
        every { moviesRepository.getTrendingMovies("day") } returns flow { throw RuntimeException("Network error") }

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).message)

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
