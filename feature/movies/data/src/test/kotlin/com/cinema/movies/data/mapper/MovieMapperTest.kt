package com.cinema.movies.data.mapper

import com.cinema.movies.data.mapper.MovieMapper.toDomain
import com.cinema.movies.data.mapper.MovieMapper.toDomainList
import com.cinema.movies.data.network.model.GenreDto
import com.cinema.movies.data.network.model.MovieDetailResponse
import com.cinema.movies.data.network.model.MovieDto
import com.cinema.movies.data.network.model.ProductionCompanyDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `MovieDto toDomain maps all fields correctly`() {
        val dto = createMovieDto()

        val movie = dto.toDomain()

        assertEquals(1, movie.id)
        assertEquals("Test Movie", movie.title)
        assertEquals("Test overview", movie.overview)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", movie.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/original/backdrop.jpg", movie.backdropUrl)
        assertEquals("2024-01-15", movie.releaseDate)
        assertEquals(8.5, movie.voteAverage, 0.01)
        assertEquals(1000, movie.voteCount)
        assertEquals(100.0, movie.popularity, 0.01)
    }

    @Test
    fun `MovieDto toDomain handles null posterPath`() {
        val dto = createMovieDto(posterPath = null)

        val movie = dto.toDomain()

        assertEquals(null, movie.posterUrl)
    }

    @Test
    fun `MovieDto toDomain handles null backdropPath`() {
        val dto = createMovieDto(backdropPath = null)

        val movie = dto.toDomain()

        assertEquals(null, movie.backdropUrl)
    }

    @Test
    fun `MovieDto toDomain handles null releaseDate`() {
        val dto = createMovieDto(releaseDate = null)

        val movie = dto.toDomain()

        assertEquals("", movie.releaseDate)
    }

    @Test
    fun `List toDomainList maps all items`() {
        val dtos = listOf(
            createMovieDto(id = 1, title = "Movie 1"),
            createMovieDto(id = 2, title = "Movie 2"),
            createMovieDto(id = 3, title = "Movie 3")
        )

        val movies = dtos.toDomainList()

        assertEquals(3, movies.size)
        assertEquals("Movie 1", movies[0].title)
        assertEquals("Movie 2", movies[1].title)
        assertEquals("Movie 3", movies[2].title)
    }

    @Test
    fun `empty list toDomainList returns empty list`() {
        val dtos = emptyList<MovieDto>()

        val movies = dtos.toDomainList()

        assertTrue(movies.isEmpty())
    }

    @Test
    fun `MovieDetailResponse toDomain maps all fields correctly`() {
        val response = createMovieDetailResponse()

        val detail = response.toDomain()

        assertEquals(1, detail.id)
        assertEquals("Test Movie", detail.title)
        assertEquals("Test overview", detail.overview)
        assertEquals(120, detail.runtime)
        assertEquals("Released", detail.status)
        assertEquals("Test tagline", detail.tagline)
        assertEquals(100_000_000L, detail.budget)
        assertEquals(500_000_000L, detail.revenue)
        assertEquals(2, detail.genres.size)
        assertEquals("Action", detail.genres[0].name)
        assertEquals(2, detail.productionCompanies.size)
        assertEquals("Company A", detail.productionCompanies[0])
    }

    @Test
    fun `MovieDetailResponse toDomain handles null genres`() {
        val response = createMovieDetailResponse(genres = null)

        val detail = response.toDomain()

        assertTrue(detail.genres.isEmpty())
    }

    @Test
    fun `MovieDetailResponse toDomain handles null productionCompanies`() {
        val response = createMovieDetailResponse(productionCompanies = null)

        val detail = response.toDomain()

        assertTrue(detail.productionCompanies.isEmpty())
    }

    @Test
    fun `GenreDto toDomain maps correctly`() {
        val dto = GenreDto(id = 28, name = "Action")

        val genre = dto.toDomain()

        assertEquals(28, genre.id)
        assertEquals("Action", genre.name)
    }

    private fun createMovieDto(
        id: Int = 1,
        title: String = "Test Movie",
        posterPath: String? = "/poster.jpg",
        backdropPath: String? = "/backdrop.jpg",
        releaseDate: String? = "2024-01-15"
    ) = MovieDto(
        id = id,
        title = title,
        overview = "Test overview",
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = 8.5,
        voteCount = 1000,
        popularity = 100.0,
        adult = false,
        originalLanguage = "en",
        originalTitle = title,
        genreIds = listOf(28, 12)
    )

    private fun createMovieDetailResponse(
        genres: List<GenreDto>? = listOf(
            GenreDto(28, "Action"),
            GenreDto(12, "Adventure")
        ),
        productionCompanies: List<ProductionCompanyDto>? = listOf(
            ProductionCompanyDto(1, "Company A", null, "US"),
            ProductionCompanyDto(2, "Company B", null, "UK")
        )
    ) = MovieDetailResponse(
        id = 1,
        title = "Test Movie",
        overview = "Test overview",
        posterPath = "/poster.jpg",
        backdropPath = "/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.5,
        voteCount = 1000,
        popularity = 100.0,
        originalLanguage = "en",
        originalTitle = "Test Movie",
        runtime = 120,
        status = "Released",
        tagline = "Test tagline",
        budget = 100_000_000L,
        revenue = 500_000_000L,
        genres = genres,
        productionCompanies = productionCompanies,
        homepage = "https://test.com",
        imdbId = "tt1234567"
    )
}
