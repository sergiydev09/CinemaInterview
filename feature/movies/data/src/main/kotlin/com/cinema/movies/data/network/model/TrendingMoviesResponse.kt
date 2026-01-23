package com.cinema.movies.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response model for the trending movies API endpoint.
 */
@JsonClass(generateAdapter = true)
data class TrendingMoviesResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<MovieDto>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

/**
 * Data Transfer Object for a movie from the TMDB API.
 */
@JsonClass(generateAdapter = true)
data class MovieDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "overview") val overview: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "backdrop_path") val backdropPath: String?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "vote_average") val voteAverage: Double,
    @Json(name = "vote_count") val voteCount: Int,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "adult") val adult: Boolean,
    @Json(name = "original_language") val originalLanguage: String,
    @Json(name = "original_title") val originalTitle: String,
    @Json(name = "genre_ids") val genreIds: List<Int>,
    @Json(name = "media_type") val mediaType: String? = null
)
