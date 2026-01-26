package com.cinema.movies.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MovieDetailDTO(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("overview") val overview: String,
    @SerialName("poster_path") val posterPath: String?,
    @SerialName("backdrop_path") val backdropPath: String?,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    @SerialName("popularity") val popularity: Double,
    @SerialName("runtime") val runtime: Int?,
    @SerialName("status") val status: String?,
    @SerialName("tagline") val tagline: String?,
    @SerialName("budget") val budget: Long,
    @SerialName("revenue") val revenue: Long,
    @SerialName("homepage") val homepage: String?,
    @SerialName("imdb_id") val imdbId: String?,
    @SerialName("original_language") val originalLanguage: String,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("genres") val genres: List<GenreDTO>? = null,
    @SerialName("production_companies") val productionCompanies: List<ProductionCompanyDTO>? = null
)

@Serializable
data class GenreDTO(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String
)

@Serializable
data class ProductionCompanyDTO(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("logo_path") val logoPath: String?,
    @SerialName("origin_country") val originCountry: String
)
