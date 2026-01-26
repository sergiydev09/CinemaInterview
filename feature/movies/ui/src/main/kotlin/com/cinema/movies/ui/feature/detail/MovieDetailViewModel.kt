package com.cinema.movies.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.core.favorites.domain.usecase.ToggleMovieFavoriteUseCase
import com.cinema.movies.domain.mapper.toFavoriteMovie
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.usecase.GetMovieDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MovieDetailUiState(
    val isLoading: Boolean = false,
    val movie: MovieDetail? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetailUseCase: GetMovieDetailUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val toggleMovieFavoriteUseCase: ToggleMovieFavoriteUseCase
) : ViewModel() {

    private val movieId: Int = savedStateHandle.get<Int>(ARG_MOVIE_ID) ?: 0

    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    init {
        loadMovieDetail()
        observeFavoriteStatus()
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesRepository.isMovieFavorite(movieId).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    fun loadMovieDetail() {
        viewModelScope.launch {
            getMovieDetailUseCase(movieId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, movie = result.data) }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    fun toggleFavorite() {
        _uiState.value.movie?.run {
            viewModelScope.launch {
                toggleMovieFavoriteUseCase(toFavoriteMovie())
            }
        }
    }

    fun retry() {
        loadMovieDetail()
    }

    companion object {
        const val ARG_MOVIE_ID = "movieId"
    }
}
