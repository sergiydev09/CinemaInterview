package com.cinema.people.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PersonDetailDTO(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("biography") val biography: String?,
    @SerialName("birthday") val birthday: String?,
    @SerialName("deathday") val deathday: String?,
    @SerialName("place_of_birth") val placeOfBirth: String?,
    @SerialName("profile_path") val profilePath: String?,
    @SerialName("popularity") val popularity: Double,
    @SerialName("known_for_department") val knownForDepartment: String?,
    @SerialName("homepage") val homepage: String?,
    @SerialName("imdb_id") val imdbId: String?,
    @SerialName("also_known_as") val alsoKnownAs: List<String>? = null,
    @SerialName("adult") val adult: Boolean,
    @SerialName("gender") val gender: Int
)
