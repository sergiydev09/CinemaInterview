package com.cinema.core.favorites.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePersonEntity(
    val id: Int,
    val name: String,
    val profileUrl: String?
)
