package com.cinema.movies.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.usecase.GetTrendingMoviesUseCase
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

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getTrendingMoviesUseCase: GetTrendingMoviesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoviesUiState())
    val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            getTrendingMoviesUseCase(_uiState.value.selectedTimeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { state ->
                            // Only show loading if we don't have data yet
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

    fun onTimeWindowChanged(timeWindow: TimeWindow) {
        if (timeWindow != _uiState.value.selectedTimeWindow) {
            _uiState.update { it.copy(selectedTimeWindow = timeWindow) }
            loadMovies()
        }
    }

    fun retry() {
        loadMovies()
    }
}
