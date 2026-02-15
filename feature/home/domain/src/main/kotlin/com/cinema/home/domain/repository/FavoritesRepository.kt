package com.cinema.home.domain.repository

import com.cinema.home.domain.model.FavoriteMovie
import com.cinema.home.domain.model.FavoritePerson
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    val favoriteMovies: Flow<List<FavoriteMovie>>
    val favoritePeople: Flow<List<FavoritePerson>>
}
