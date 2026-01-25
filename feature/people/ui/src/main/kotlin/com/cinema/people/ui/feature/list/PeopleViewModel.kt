package com.cinema.people.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.usecase.TogglePersonFavoriteUseCase
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.usecase.GetTrendingPeopleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the people screen.
 */
data class PeopleUiState(
    val isLoading: Boolean = false,
    val people: List<Person> = emptyList(),
    val error: String? = null,
    val selectedTimeWindow: TimeWindow = TimeWindow.DAY
)

/**
 * ViewModel for the people screen.
 * Handles loading and displaying trending people.
 */
@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val getTrendingPeopleUseCase: GetTrendingPeopleUseCase,
    private val togglePersonFavoriteUseCase: TogglePersonFavoriteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    init {
        loadPeople()
    }

    fun toggleFavorite(person: Person) {
        viewModelScope.launch {
            togglePersonFavoriteUseCase(person.toFavoritePerson())
        }
    }

    private fun Person.toFavoritePerson(): FavoritePerson = FavoritePerson(
        id = id,
        name = name,
        profileUrl = profileUrl
    )

    /**
     * Loads trending people based on the current time window.
     */
    fun loadPeople() {
        viewModelScope.launch {
            getTrendingPeopleUseCase(_uiState.value.selectedTimeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { state ->
                            // Only show loading if we don't have data yet
                            state.copy(
                                isLoading = state.people.isEmpty(),
                                error = null
                            )
                        }
                    }

                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, people = result.data) }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    /**
     * Changes the time window filter and reloads people.
     */
    fun onTimeWindowChanged(timeWindow: TimeWindow) {
        if (timeWindow != _uiState.value.selectedTimeWindow) {
            _uiState.update { it.copy(selectedTimeWindow = timeWindow) }
            loadPeople()
        }
    }

    /**
     * Retries loading people after an error.
     */
    fun retry() {
        loadPeople()
    }
}
