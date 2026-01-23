package com.cinema.people.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Response model for the trending people API endpoint.
 */
@JsonClass(generateAdapter = true)
data class TrendingPeopleResponse(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<PersonDto>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

/**
 * Data Transfer Object for a person from the TMDB API.
 */
@JsonClass(generateAdapter = true)
data class PersonDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "known_for_department") val knownForDepartment: String?,
    @Json(name = "gender") val gender: Int,
    @Json(name = "adult") val adult: Boolean,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "known_for") val knownFor: List<KnownForDto>? = null
)

/**
 * DTO representing a movie or TV show the person is known for.
 */
@JsonClass(generateAdapter = true)
data class KnownForDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "media_type") val mediaType: String,
    @Json(name = "poster_path") val posterPath: String?,
    @Json(name = "overview") val overview: String?,
    @Json(name = "vote_average") val voteAverage: Double?
)
