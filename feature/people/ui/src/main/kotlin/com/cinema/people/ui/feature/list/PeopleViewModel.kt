package com.cinema.people.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.usecase.TogglePersonFavoriteUseCase
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.usecase.GetTrendingPeopleUseCase
import com.cinema.people.ui.ai.PeopleAIIntentHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PeopleUiState(
    val isLoading: Boolean = false,
    val people: List<Person> = emptyList(),
    val error: String? = null,
    val selectedTimeWindow: TimeWindow = TimeWindow.DAY
)

sealed interface PeopleIntent : AIIntent {
    data class ChangeTimeWindow(val timeWindow: TimeWindow) : PeopleIntent
    data class ToggleFavorite(val person: Person) : PeopleIntent
    data object Retry : PeopleIntent
}

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val getTrendingPeopleUseCase: GetTrendingPeopleUseCase,
    private val togglePersonFavoriteUseCase: TogglePersonFavoriteUseCase,
    private val peopleAIIntentHandler: PeopleAIIntentHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    init {
        observeAIIntents()
        loadPeople()
    }

    fun handleIntent(intent: PeopleIntent) {
        when (intent) {
            is PeopleIntent.ChangeTimeWindow -> changeTimeWindow(intent.timeWindow)
            is PeopleIntent.ToggleFavorite -> toggleFavorite(intent.person)
            is PeopleIntent.Retry -> loadPeople()
        }
    }

    private fun observeAIIntents() {
        viewModelScope.launch {
            peopleAIIntentHandler.intents.collect { intent ->
                (intent as? PeopleIntent)?.let(::handleIntent)
            }
        }
    }

    private fun toggleFavorite(person: Person) {
        viewModelScope.launch {
            togglePersonFavoriteUseCase(person.toFavoritePerson())
        }
    }

    private fun Person.toFavoritePerson(): FavoritePerson = FavoritePerson(
        id = id,
        name = name,
        profileUrl = profileUrl
    )

    private fun loadPeople() {
        viewModelScope.launch {
            getTrendingPeopleUseCase(_uiState.value.selectedTimeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { state ->
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

    private fun changeTimeWindow(timeWindow: TimeWindow) {
        if (timeWindow != _uiState.value.selectedTimeWindow) {
            _uiState.update { it.copy(selectedTimeWindow = timeWindow) }
            loadPeople()
        }
    }
}
