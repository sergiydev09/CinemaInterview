package com.cinema.core.favorites.domain.usecase

import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleMovieFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(movie: FavoriteMovie) {
        val isFavorite = favoritesRepository.isMovieFavorite(movie.id).first()
        if (isFavorite) {
            favoritesRepository.removeFavoriteMovie(movie.id)
        } else {
            favoritesRepository.addFavoriteMovie(movie)
        }
    }
}
