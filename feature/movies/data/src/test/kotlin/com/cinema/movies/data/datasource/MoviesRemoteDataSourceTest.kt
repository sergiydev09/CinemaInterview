package com.cinema.movies.data.datasource

import com.cinema.movies.data.network.MoviesApiService
import com.cinema.movies.data.network.model.MovieDetailResponse
import com.cinema.movies.data.network.model.MovieDto
import com.cinema.movies.data.network.model.TrendingMoviesResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MoviesRemoteDataSourceTest {

    private lateinit var apiService: MoviesApiService
    private lateinit var dataSource: MoviesRemoteDataSource

    @Before
    fun setup() {
        apiService = mockk()
        dataSource = MoviesRemoteDataSource(apiService)
    }

    @Test
    fun `getTrendingMovies returns movies from api`() = runTest {
        val expectedMovies = listOf(createMovieDto(1), createMovieDto(2))
        val response = TrendingMoviesResponse(
            page = 1,
            results = expectedMovies,
            totalPages = 1,
            totalResults = 2
        )
        coEvery { apiService.getTrendingMovies("day") } returns response

        val result = dataSource.getTrendingMovies("day")

        assertEquals(expectedMovies, result)
        coVerify { apiService.getTrendingMovies("day") }
    }

    @Test
    fun `getTrendingMovies passes timeWindow to api`() = runTest {
        val response = TrendingMoviesResponse(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )
        coEvery { apiService.getTrendingMovies("week") } returns response

        dataSource.getTrendingMovies("week")

        coVerify { apiService.getTrendingMovies("week") }
    }

    @Test
    fun `getMovieDetail returns detail from api`() = runTest {
        val expected = createMovieDetailResponse()
        coEvery { apiService.getMovieDetail(123) } returns expected

        val result = dataSource.getMovieDetail(123)

        assertEquals(expected, result)
        coVerify { apiService.getMovieDetail(123) }
    }

    @Test(expected = Exception::class)
    fun `getTrendingMovies throws exception when api fails`() = runTest {
        coEvery { apiService.getTrendingMovies(any()) } throws Exception("Network error")

        dataSource.getTrendingMovies("day")
    }

    @Test(expected = Exception::class)
    fun `getMovieDetail throws exception when api fails`() = runTest {
        coEvery { apiService.getMovieDetail(any()) } throws Exception("Network error")

        dataSource.getMovieDetail(123)
    }

    private fun createMovieDto(id: Int) = MovieDto(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0,
        adult = false,
        originalLanguage = "en",
        originalTitle = "Movie $id",
        genreIds = listOf(28)
    )

    private fun createMovieDetailResponse() = MovieDetailResponse(
        id = 123,
        title = "Test Movie",
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0,
        originalLanguage = "en",
        originalTitle = "Test Movie",
        runtime = 120,
        status = "Released",
        tagline = "Tagline",
        budget = 100_000_000L,
        revenue = 500_000_000L,
        genres = emptyList(),
        productionCompanies = emptyList(),
        homepage = null,
        imdbId = null
    )
}
