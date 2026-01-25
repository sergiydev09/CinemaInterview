package com.cinema.people.ui.feature.list

import androidx.compose.runtime.Composable
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

    PeopleContent(
        uiState = uiState,
        onPersonClick = onPersonClick,
        onTimeWindowChanged = viewModel::onTimeWindowChanged,
        onRetry = viewModel::retry
    )
}
