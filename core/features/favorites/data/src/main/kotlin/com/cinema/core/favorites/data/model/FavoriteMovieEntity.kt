package com.cinema.core.favorites.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteMovieEntity(
    val id: Int,
    val title: String,
    val posterUrl: String?,
    val releaseDate: String?
)
