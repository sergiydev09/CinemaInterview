package com.cinema.movies.domain.mapper

import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.movies.domain.model.MovieDetail

fun MovieDetail.toFavoriteMovie(): FavoriteMovie = FavoriteMovie(
    id = id,
    title = title,
    posterUrl = posterUrl,
    releaseDate = releaseDate
)