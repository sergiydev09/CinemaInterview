package com.cinema.core.favorites.domain.repository

import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.model.FavoritePerson
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    val favoriteMovies: Flow<List<FavoriteMovie>>
    val favoritePeople: Flow<List<FavoritePerson>>

    fun isMovieFavorite(movieId: Int): Flow<Boolean>
    fun isPersonFavorite(personId: Int): Flow<Boolean>

    suspend fun addFavoriteMovie(movie: FavoriteMovie)
    suspend fun removeFavoriteMovie(movieId: Int)
    suspend fun addFavoritePerson(person: FavoritePerson)
    suspend fun removeFavoritePerson(personId: Int)
}
