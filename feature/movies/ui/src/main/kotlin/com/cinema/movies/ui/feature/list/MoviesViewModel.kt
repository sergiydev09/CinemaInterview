package com.cinema.movies.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.usecase.ToggleMovieFavoriteUseCase
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.usecase.GetTrendingMoviesUseCase
import com.cinema.movies.ui.ai.MoviesAIIntentHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val selectedTimeWindow: TimeWindow = TimeWindow.DAY
)

sealed interface MoviesIntent : AIIntent {
    data class ChangeTimeWindow(val timeWindow: TimeWindow) : MoviesIntent
    data class ToggleFavorite(val movie: Movie) : MoviesIntent
    data object Retry : MoviesIntent
}

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val toggleMovieFavoriteUseCase: ToggleMovieFavoriteUseCase,
    private val moviesAIIntentHandler: MoviesAIIntentHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        observeAIIntents()
        loadMovies()
    }

    fun handleIntent(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.ChangeTimeWindow -> changeTimeWindow(intent.timeWindow)
            is MoviesIntent.ToggleFavorite -> toggleFavorite(intent.movie)
            is MoviesIntent.Retry -> loadMovies()
        }
    }

    private fun observeAIIntents() {
        viewModelScope.launch {
            moviesAIIntentHandler.intents.collect { intent ->
                (intent as? MoviesIntent)?.let(::handleIntent)
            }
        }
    }

    private fun toggleFavorite(movie: Movie) {
        viewModelScope.launch {
            toggleMovieFavoriteUseCase(movie.toFavoriteMovie())
        }
    }

    private fun Movie.toFavoriteMovie(): FavoriteMovie = FavoriteMovie(
        id = id,
        title = title,
        posterUrl = posterUrl,
        releaseDate = releaseDate
    )

    private fun loadMovies() {
        viewModelScope.launch {
            getTrendingMoviesUseCase(_uiState.value.selectedTimeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = state.movies.isEmpty(),
                                error = null
                            )
                        }
                    }

                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, movies = result.data) }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    private fun changeTimeWindow(timeWindow: TimeWindow) {
        if (timeWindow != _uiState.value.selectedTimeWindow) {
            _uiState.update { it.copy(selectedTimeWindow = timeWindow) }
            loadMovies()
        }
    }
}
