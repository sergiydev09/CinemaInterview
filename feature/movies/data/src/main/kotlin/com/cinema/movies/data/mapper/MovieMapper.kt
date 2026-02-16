package com.cinema.movies.data.mapper

import com.cinema.core.data.util.ImageUrlBuilder
import com.cinema.movies.data.network.model.GenreDTO
import com.cinema.movies.data.network.model.MovieDTO
import com.cinema.movies.data.network.model.MovieDetailDTO
import com.cinema.movies.domain.model.Genre
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail

object MovieMapper {

    private val TMDB_GENRES = mapOf(
        28 to "Action", 12 to "Adventure", 16 to "Animation",
        35 to "Comedy", 80 to "Crime", 99 to "Documentary",
        18 to "Drama", 10751 to "Family", 14 to "Fantasy",
        36 to "History", 27 to "Horror", 10402 to "Music",
        9648 to "Mystery", 10749 to "Romance", 878 to "Science Fiction",
        10770 to "TV Movie", 53 to "Thriller", 10752 to "War", 37 to "Western"
    )

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
            popularity = popularity,
            genreNames = genreIds.mapNotNull { TMDB_GENRES[it] }
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
