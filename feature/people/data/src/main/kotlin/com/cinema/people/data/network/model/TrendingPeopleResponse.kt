package com.cinema.people.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrendingPeopleResponse(
    @SerialName("page") val page: Int,
    @SerialName("results") val results: List<PersonDto>,
    @SerialName("total_pages") val totalPages: Int,
    @SerialName("total_results") val totalResults: Int
)

@Serializable
data class PersonDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("profile_path") val profilePath: String?,
    @SerialName("popularity") val popularity: Double,
    @SerialName("known_for_department") val knownForDepartment: String? = null,
    @SerialName("gender") val gender: Int,
    @SerialName("adult") val adult: Boolean,
    @SerialName("media_type") val mediaType: String? = null,
    @SerialName("known_for") val knownFor: List<KnownForDto>? = null
)

@Serializable
data class KnownForDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("media_type") val mediaType: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("overview") val overview: String?,
    @SerialName("vote_average") val voteAverage: Double?
)
