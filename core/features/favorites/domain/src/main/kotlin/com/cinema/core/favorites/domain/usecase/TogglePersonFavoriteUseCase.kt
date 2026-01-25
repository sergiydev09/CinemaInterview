package com.cinema.core.favorites.domain.usecase

import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class TogglePersonFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(person: FavoritePerson) {
        val isFavorite = favoritesRepository.isPersonFavorite(person.id).first()
        if (isFavorite) {
            favoritesRepository.removeFavoritePerson(person.id)
        } else {
            favoritesRepository.addFavoritePerson(person)
        }
    }
}
