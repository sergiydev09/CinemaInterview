package com.cinema.core.favorites.data.mapper

import com.cinema.core.favorites.data.model.FavoriteMovieEntity
import com.cinema.core.favorites.data.model.FavoritePersonEntity
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.model.FavoritePerson

fun FavoriteMovieEntity.toDomain(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate
)

fun FavoriteMovie.toEntity(): FavoriteMovieEntity = FavoriteMovieEntity(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate
)

fun FavoritePersonEntity.toDomain(): FavoritePerson = FavoritePerson(
    id = id,
    name = name,
    profileUrl = profileUrl
)

fun FavoritePerson.toEntity(): FavoritePersonEntity = FavoritePersonEntity(
    id = id,
    name = name,
    profileUrl = profileUrl
)
