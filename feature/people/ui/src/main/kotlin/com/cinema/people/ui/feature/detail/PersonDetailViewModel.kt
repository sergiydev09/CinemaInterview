package com.cinema.people.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.core.favorites.domain.usecase.TogglePersonFavoriteUseCase
import com.cinema.people.domain.mapper.toFavoritePerson
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.usecase.GetPersonDetailUseCase
import com.cinema.people.ui.ai.PeopleAIIntentHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PersonDetailUiState(
    val isLoading: Boolean = false,
    val person: PersonDetail? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

sealed interface PersonDetailIntent : AIIntent {
    data object ToggleFavorite : PersonDetailIntent
    data object Retry : PersonDetailIntent
}

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPersonDetailUseCase: GetPersonDetailUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val togglePersonFavoriteUseCase: TogglePersonFavoriteUseCase,
    private val peopleAIIntentHandler: PeopleAIIntentHandler
) : ViewModel() {

    private val personId: Int = savedStateHandle.get<Int>(ARG_PERSON_ID) ?: 0

    private val _uiState = MutableStateFlow(PersonDetailUiState())
    val uiState: StateFlow<PersonDetailUiState> = _uiState.asStateFlow()

    init {
        loadPersonDetail()
        observeFavoriteStatus()
        observeAIIntents()
    }

    fun handleIntent(intent: PersonDetailIntent) {
        when (intent) {
            is PersonDetailIntent.ToggleFavorite -> toggleFavorite()
            is PersonDetailIntent.Retry -> loadPersonDetail()
        }
    }

    private fun observeAIIntents() {
        viewModelScope.launch {
            peopleAIIntentHandler.intents.collect { intent ->
                (intent as? PersonDetailIntent)?.let(::handleIntent)
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesRepository.isPersonFavorite(personId).collect { isFavorite ->
                _uiState.update { it.copy(isFavorite = isFavorite) }
            }
        }
    }

    private fun loadPersonDetail() {
        viewModelScope.launch {
            getPersonDetailUseCase(personId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Result.Success -> {
                        _uiState.update { it.copy(isLoading = false, person = result.data) }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }

    private fun toggleFavorite() {
        _uiState.value.person?.run {
            viewModelScope.launch {
                togglePersonFavoriteUseCase(toFavoritePerson())
            }
        }
    }

    companion object {
        const val ARG_PERSON_ID = "personId"
    }
}
