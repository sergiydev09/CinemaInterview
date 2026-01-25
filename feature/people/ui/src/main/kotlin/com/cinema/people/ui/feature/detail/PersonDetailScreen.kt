package com.cinema.people.ui.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.core.ui.compose.ErrorContent
import com.cinema.core.ui.compose.LoadingContent
import com.cinema.people.ui.feature.detail.views.PersonDetailContent

@Composable
fun PersonDetailScreen(
    onBackClick: () -> Unit,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val person = uiState.person
    val error = uiState.error

    when {
        uiState.isLoading -> {
            LoadingContent()
        }
        error != null -> {
            ErrorContent(
                message = error,
                onRetry = viewModel::retry
            )
        }
        person != null -> {
            PersonDetailContent(
                person = person,
                isFavorite = uiState.isFavorite,
                onFavoriteClick = viewModel::toggleFavorite,
                onBackClick = onBackClick
            )
        }
    }
}
