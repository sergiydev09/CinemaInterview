package com.cinema.people.ui.feature.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.people.ui.feature.list.views.PeopleContent

@Composable
fun PeopleScreen(
    onPersonClick: (Int) -> Unit,
    viewModel: PeopleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { personId ->
            onPersonClick(personId)
        }
    }

    PeopleContent(
        uiState = uiState,
        onPersonClick = onPersonClick,
        onFavoriteClick = { viewModel.handleIntent(PeopleIntent.ToggleFavorite(it)) },
        onTimeWindowChanged = { viewModel.handleIntent(PeopleIntent.ChangeTimeWindow(it)) },
        onClearFilter = { viewModel.handleIntent(PeopleIntent.ClearFilter) },
        onRetry = { viewModel.handleIntent(PeopleIntent.Retry) }
    )
}
