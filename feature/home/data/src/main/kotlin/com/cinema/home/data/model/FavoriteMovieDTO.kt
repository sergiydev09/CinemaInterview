package com.cinema.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteMovieDTO(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String?
)
