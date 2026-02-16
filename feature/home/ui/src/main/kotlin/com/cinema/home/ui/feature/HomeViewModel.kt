package com.cinema.home.ui.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.home.domain.model.FavoriteMovie
import com.cinema.home.domain.model.FavoritePerson
import com.cinema.home.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val favoriteMovies: List<FavoriteMovie> = emptyList(),
    val favoritePeople: List<FavoritePerson> = emptyList()
) {
    val hasFavorites: Boolean
        get() = favoriteMovies.isNotEmpty() || favoritePeople.isNotEmpty()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    favoritesRepository: FavoritesRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        favoritesRepository.favoriteMovies,
        favoritesRepository.favoritePeople
    ) { movies, people ->
        HomeUiState(
            favoriteMovies = movies,
            favoritePeople = people
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}
