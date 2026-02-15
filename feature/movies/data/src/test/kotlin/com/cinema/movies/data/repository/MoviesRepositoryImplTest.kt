package com.cinema.movies.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.movies.data.datasource.MoviesRemoteDataSource
import com.cinema.movies.data.network.model.MovieDTO
import com.cinema.movies.data.network.model.MovieDetailDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MoviesRepositoryImplTest {

    private lateinit var remoteDataSource: MoviesRemoteDataSource
    private lateinit var secureLocalDataSource: SecureLocalDataSource
    private lateinit var repository: MoviesRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mockk()
        secureLocalDataSource = mockk {
            every { observe<Any>(any(), any()) } returns flowOf(null)
        }
        repository = MoviesRepositoryImpl(remoteDataSource, secureLocalDataSource)
    }

    @Test
    fun `getTrendingMovies returns mapped movies from data source`() = runTest {
        val movieDtos = listOf(createMovieDto(1), createMovieDto(2))
        coEvery { remoteDataSource.getTrendingMovies("day") } returns movieDtos

        val result = repository.getTrendingMovies("day").first()

        assertEquals(2, result.size)
        coVerify { remoteDataSource.getTrendingMovies("day") }
    }

    @Test
    fun `getTrendingMovies maps dto to domain correctly`() = runTest {
        val movieDto = createMovieDto(1, "Test Movie")
        coEvery { remoteDataSource.getTrendingMovies("day") } returns listOf(movieDto)

        val result = repository.getTrendingMovies("day").first()

        val movie = result.first()
        assertEquals(1, movie.id)
        assertEquals("Test Movie", movie.title)
    }

    @Test
    fun `getMovieDetail returns mapped movie detail from data source`() = runTest {
        val detailResponse = createMovieDetailResponse()
        coEvery { remoteDataSource.getMovieDetail(123) } returns detailResponse

        val result = repository.getMovieDetail(123)

        assertEquals(123, result.id)
        assertEquals("Test Movie", result.title)
        coVerify { remoteDataSource.getMovieDetail(123) }
    }

    @Test(expected = Exception::class)
    fun `getTrendingMovies throws exception from data source`() = runTest {
        coEvery { remoteDataSource.getTrendingMovies("day") } throws Exception("Network error")

        repository.getTrendingMovies("day").first()
    }

    @Test(expected = Exception::class)
    fun `getMovieDetail throws exception from data source`() = runTest {
        coEvery { remoteDataSource.getMovieDetail(123) } throws Exception("Not found")

        repository.getMovieDetail(123)
    }

    private fun createMovieDto(id: Int, title: String = "Movie $id") = MovieDTO(
        id = id,
        title = title,
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0,
        adult = false,
        originalLanguage = "en",
        originalTitle = title,
        genreIds = listOf(28)
    )

    private fun createMovieDetailResponse() = MovieDetailDTO(
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
