package com.cinema.movies.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTrendingMoviesUseCaseTest {

    private lateinit var repository: MoviesRepository
    private lateinit var useCase: GetTrendingMoviesUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetTrendingMoviesUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct timeWindow`() = runTest {
        val movies = listOf(createMovie(1))
        coEvery { repository.getTrendingMovies("week") } returns movies

        useCase(TimeWindow.WEEK).test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        coVerify { repository.getTrendingMovies("week") }
    }

    @Test
    fun `invoke uses default timeWindow when not specified`() = runTest {
        val movies = listOf(createMovie(1))
        coEvery { repository.getTrendingMovies("day") } returns movies

        useCase().test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        coVerify { repository.getTrendingMovies("day") }
    }

    @Test
    fun `invoke emits Loading then Success with movies`() = runTest {
        val movies = listOf(createMovie(1), createMovie(2))
        coEvery { repository.getTrendingMovies("day") } returns movies

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val success = awaitItem()
            assertTrue(success is Result.Success)
            assertEquals(2, (success as Result.Success).data.size)

            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Loading then Error on exception`() = runTest {
        coEvery { repository.getTrendingMovies("day") } throws RuntimeException("Network error")

        useCase(TimeWindow.DAY).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Network error", (error as Result.Error).message)

            awaitComplete()
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
