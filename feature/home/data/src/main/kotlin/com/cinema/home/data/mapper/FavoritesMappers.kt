package com.cinema.home.data.mapper

import com.cinema.home.data.model.FavoriteMovieDTO
import com.cinema.home.data.model.FavoritePersonDTO
import com.cinema.home.domain.model.FavoriteMovie
import com.cinema.home.domain.model.FavoritePerson

fun FavoriteMovieDTO.toDomain(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate
)

fun FavoritePersonDTO.toDomain(): FavoritePerson = FavoritePerson(
    id = id,
    name = name,
    profileUrl = profileUrl
)
