package com.cinema.home.domain.model

data class FavoriteMovie(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String?
)
