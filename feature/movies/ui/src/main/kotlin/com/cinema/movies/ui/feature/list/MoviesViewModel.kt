package com.cinema.movies.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
import com.cinema.movies.domain.usecase.GetTrendingMoviesUseCase
import com.cinema.movies.ui.ai.MoviesAIIntentHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MoviesUiState(
    val isLoading: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null,
    val selectedTimeWindow: TimeWindow = TimeWindow.DAY,
    val filterIds: Set<Int>? = null,
    val activeFilterLabel: String? = null
) {
    val displayedMovies: List<Movie>
        get() = filterIds?.let { ids -> movies.filter { it.id in ids } } ?: movies
}

sealed interface MoviesIntent : AIIntent {
    data class ChangeTimeWindow(val timeWindow: TimeWindow) : MoviesIntent
    data class ToggleFavorite(val movie: Movie) : MoviesIntent
    data object Retry : MoviesIntent
    data class ApplyFilter(val matchedIds: List<Int>, val label: String) : MoviesIntent
    data object ClearFilter : MoviesIntent
    data class NavigateToDetail(val movieId: Int) : MoviesIntent
}

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase,
    private val moviesRepository: MoviesRepository,
    private val moviesAIIntentHandler: MoviesAIIntentHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<Int> = _navigationEvent.asSharedFlow()

    private var moviesJob: Job? = null

    init {
        observeAIIntents()
        loadMovies(TimeWindow.DAY)
    }

    fun handleIntent(intent: MoviesIntent) {
        when (intent) {
            is MoviesIntent.ChangeTimeWindow -> {
                if (intent.timeWindow != _uiState.value.selectedTimeWindow) {
                    _uiState.update {
                        it.copy(
                            selectedTimeWindow = intent.timeWindow,
                            filterIds = null,
                            activeFilterLabel = null
                        )
                    }
                    loadMovies(intent.timeWindow)
                }
            }

            is MoviesIntent.ToggleFavorite -> toggleFavorite(intent.movie)
            is MoviesIntent.Retry -> loadMovies(_uiState.value.selectedTimeWindow)
            is MoviesIntent.ApplyFilter -> _uiState.update {
                it.copy(filterIds = intent.matchedIds.toSet(), activeFilterLabel = intent.label)
            }

            is MoviesIntent.ClearFilter -> _uiState.update {
                it.copy(filterIds = null, activeFilterLabel = null)
            }

            is MoviesIntent.NavigateToDetail -> _navigationEvent.tryEmit(intent.movieId)
        }
    }

    private fun loadMovies(timeWindow: TimeWindow) {
        moviesJob?.cancel()
        moviesJob = viewModelScope.launch {
            getTrendingMoviesUseCase(timeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { state ->
                        state.copy(isLoading = state.movies.isEmpty(), error = null)
                    }

                    is Result.Success -> {
                        moviesAIIntentHandler.setScreenData(result.data, timeWindow)
                        _uiState.update { it.copy(isLoading = false, movies = result.data) }
                    }

                    is Result.Error -> {
                        moviesAIIntentHandler.notifyScreenDataLoadCompleted()
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
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
            moviesRepository.toggleFavoriteMovie(movie.id, movie.title, movie.posterUrl, movie.releaseDate)
        }
    }
}
