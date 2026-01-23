package com.cinema.movies.domain.usecase

import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.repository.MoviesRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetMovieDetailUseCaseTest {

    private lateinit var repository: MoviesRepository
    private lateinit var useCase: GetMovieDetailUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetMovieDetailUseCase(repository)
    }

    @Test
    fun `invoke calls repository with correct movieId`() = runTest {
        val detail = createMovieDetail(123)
        coEvery { repository.getMovieDetail(123) } returns detail

        useCase(123).test {
            awaitItem() // Loading
            awaitItem() // Success
            awaitComplete()
        }

        coVerify { repository.getMovieDetail(123) }
    }

    @Test
    fun `invoke emits Loading then Success with movie detail`() = runTest {
        val detail = createMovieDetail(123)
        coEvery { repository.getMovieDetail(123) } returns detail

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
        coEvery { repository.getMovieDetail(123) } throws RuntimeException("Not found")

        useCase(123).test {
            assertTrue(awaitItem() is Result.Loading)

            val error = awaitItem()
            assertTrue(error is Result.Error)
            assertEquals("Not found", (error as Result.Error).message)

            awaitComplete()
        }
    }

    private fun createMovieDetail(id: Int) = MovieDetail(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0,
        runtime = 120,
        status = "Released",
        tagline = "Tagline",
        budget = 100_000_000L,
        revenue = 500_000_000L,
        genres = emptyList(),
        productionCompanies = emptyList()
    )
}
