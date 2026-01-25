package com.cinema.movies.ui.feature.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.core.ui.compose.ErrorContent
import com.cinema.core.ui.compose.LoadingContent
import com.cinema.movies.ui.feature.detail.views.MovieDetailContent

@Composable
fun MovieDetailScreen(
    onBackClick: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val movie = uiState.movie
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

        movie != null -> {
            MovieDetailContent(
                movie = movie,
                isFavorite = uiState.isFavorite,
                onFavoriteClick = viewModel::toggleFavorite,
                onBackClick = onBackClick
            )
        }
    }
}
