package com.cinema.movies.domain.model

data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val isFavorite: Boolean = false
)

data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val runtime: Int?,
    val status: String?,
    val tagline: String?,
    val budget: Long,
    val revenue: Long,
    val genres: List<Genre>,
    val productionCompanies: List<String>
)

data class Genre(
    val id: Int,
    val name: String
)
