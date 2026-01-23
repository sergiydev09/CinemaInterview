package com.cinema.people.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersonDetailResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "biography") val biography: String?,
    @Json(name = "birthday") val birthday: String?,
    @Json(name = "deathday") val deathday: String?,
    @Json(name = "place_of_birth") val placeOfBirth: String?,
    @Json(name = "profile_path") val profilePath: String?,
    @Json(name = "popularity") val popularity: Double,
    @Json(name = "known_for_department") val knownForDepartment: String?,
    @Json(name = "homepage") val homepage: String?,
    @Json(name = "imdb_id") val imdbId: String?,
    @Json(name = "also_known_as") val alsoKnownAs: List<String>?,
    @Json(name = "adult") val adult: Boolean,
    @Json(name = "gender") val gender: Int
)
