package com.cinema.movies.data.mapper

import com.cinema.core.data.util.ImageUrlBuilder
import com.cinema.movies.data.network.model.GenreDTO
import com.cinema.movies.data.network.model.MovieDTO
import com.cinema.movies.data.network.model.MovieDetailDTO
import com.cinema.movies.domain.model.Genre
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail

object MovieMapper {

    fun MovieDTO.toDomain(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterUrl = ImageUrlBuilder.buildPosterUrl(posterPath),
            backdropUrl = ImageUrlBuilder.buildBackdropUrl(backdropPath),
            releaseDate = releaseDate ?: "",
            voteAverage = voteAverage,
            voteCount = voteCount,
            popularity = popularity
        )
    }

    fun MovieDetailDTO.toDomain(): MovieDetail {
        return MovieDetail(
            id = id,
            title = title,
            overview = overview,
            posterUrl = ImageUrlBuilder.buildPosterUrl(posterPath),
            backdropUrl = ImageUrlBuilder.buildBackdropUrl(backdropPath),
            releaseDate = releaseDate ?: "",
            voteAverage = voteAverage,
            voteCount = voteCount,
            popularity = popularity,
            runtime = runtime,
            status = status,
            tagline = tagline,
            budget = budget,
            revenue = revenue,
            genres = genres?.map { it.toDomain() } ?: emptyList(),
            productionCompanies = productionCompanies?.map { it.name } ?: emptyList()
        )
    }

    fun GenreDTO.toDomain(): Genre {
        return Genre(id = id, name = name)
    }

    fun List<MovieDTO>.toDomainList(): List<Movie> {
        return map { it.toDomain() }
    }
}
