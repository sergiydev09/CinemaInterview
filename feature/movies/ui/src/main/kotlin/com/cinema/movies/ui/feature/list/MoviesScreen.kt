package com.cinema.movies.ui.feature.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.movies.ui.feature.list.views.MoviesContent

@Composable
fun MoviesScreen(
    onMovieClick: (Int) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MoviesContent(
        uiState = uiState,
        onMovieClick = onMovieClick,
        onTimeWindowChanged = viewModel::onTimeWindowChanged,
        onRetry = viewModel::retry
    )
}
