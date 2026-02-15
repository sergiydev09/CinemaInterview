package com.cinema.home.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FavoritePersonDTO(
    val id: Int,
    val name: String,
    val profileUrl: String?
)
