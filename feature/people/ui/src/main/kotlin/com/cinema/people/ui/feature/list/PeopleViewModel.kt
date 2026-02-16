package com.cinema.people.ui.feature.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import com.cinema.people.domain.usecase.GetTrendingPeopleUseCase
import com.cinema.people.ui.ai.PeopleAIIntentHandler
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

data class PeopleUiState(
    val isLoading: Boolean = false,
    val people: List<Person> = emptyList(),
    val error: String? = null,
    val selectedTimeWindow: TimeWindow = TimeWindow.DAY,
    val filterIds: Set<Int>? = null,
    val activeFilterLabel: String? = null
) {
    val displayedPeople: List<Person>
        get() = filterIds?.let { ids -> people.filter { it.id in ids } } ?: people
}

sealed interface PeopleIntent : AIIntent {
    data class ChangeTimeWindow(val timeWindow: TimeWindow) : PeopleIntent
    data class ToggleFavorite(val person: Person) : PeopleIntent
    data object Retry : PeopleIntent
    data class ApplyFilter(val matchedIds: List<Int>, val label: String) : PeopleIntent
    data object ClearFilter : PeopleIntent
    data class NavigateToDetail(val personId: Int) : PeopleIntent
}

@HiltViewModel
class PeopleViewModel @Inject constructor(
    private val getTrendingPeopleUseCase: GetTrendingPeopleUseCase,
    private val peopleRepository: PeopleRepository,
    private val peopleAIIntentHandler: PeopleAIIntentHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(PeopleUiState())
    val uiState: StateFlow<PeopleUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Int>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<Int> = _navigationEvent.asSharedFlow()

    private var peopleJob: Job? = null

    init {
        observeAIIntents()
        loadPeople(TimeWindow.DAY)
    }

    fun handleIntent(intent: PeopleIntent) {
        when (intent) {
            is PeopleIntent.ChangeTimeWindow -> {
                if (intent.timeWindow != _uiState.value.selectedTimeWindow) {
                    _uiState.update {
                        it.copy(
                            selectedTimeWindow = intent.timeWindow,
                            filterIds = null,
                            activeFilterLabel = null
                        )
                    }
                    loadPeople(intent.timeWindow)
                }
            }

            is PeopleIntent.ToggleFavorite -> toggleFavorite(intent.person)
            is PeopleIntent.Retry -> loadPeople(_uiState.value.selectedTimeWindow)
            is PeopleIntent.ApplyFilter -> _uiState.update {
                it.copy(filterIds = intent.matchedIds.toSet(), activeFilterLabel = intent.label)
            }

            is PeopleIntent.ClearFilter -> _uiState.update {
                it.copy(filterIds = null, activeFilterLabel = null)
            }

            is PeopleIntent.NavigateToDetail -> _navigationEvent.tryEmit(intent.personId)
        }
    }

    private fun loadPeople(timeWindow: TimeWindow) {
        peopleJob?.cancel()
        peopleJob = viewModelScope.launch {
            getTrendingPeopleUseCase(timeWindow).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { state ->
                        state.copy(isLoading = state.people.isEmpty(), error = null)
                    }

                    is Result.Success -> {
                        peopleAIIntentHandler.setScreenData(result.data, timeWindow)
                        _uiState.update { it.copy(isLoading = false, people = result.data) }
                    }

                    is Result.Error -> {
                        peopleAIIntentHandler.notifyScreenDataLoadCompleted()
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
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
            peopleRepository.toggleFavoritePerson(person.id, person.name, person.profileUrl)
        }
    }
}
