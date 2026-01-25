package com.cinema.home.ui.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cinema.home.ui.feature.views.HomeContent

@Composable
fun HomeScreen(
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onMovieClick = onMovieClick,
        onPersonClick = onPersonClick
    )
}
