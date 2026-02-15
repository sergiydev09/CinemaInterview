package com.cinema.movies.ui.feature.list.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.ui.compose.ErrorContent
import com.cinema.core.ui.compose.FilterChipBar
import com.cinema.core.ui.compose.LoadingContent
import com.cinema.core.ui.compose.TimeWindowToggle
import com.cinema.core.ui.lazylist.rememberAnimatedLazyGridState
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.ui.R
import com.cinema.movies.ui.feature.list.MoviesUiState

@Composable
fun MoviesContent(
    uiState: MoviesUiState,
    onMovieClick: (Int) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    onTimeWindowChanged: (TimeWindow) -> Unit,
    onClearFilter: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.trending_movies),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            TimeWindowToggle(
                selectedTimeWindow = uiState.selectedTimeWindow,
                onTimeWindowSelected = onTimeWindowChanged
            )
        }

        if (uiState.activeFilterLabel != null) {
            FilterChipBar(
                label = uiState.activeFilterLabel,
                resultCount = uiState.displayedMovies.size,
                onClear = onClearFilter
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val gridState = rememberLazyGridState()
            rememberAnimatedLazyGridState(gridState)

            when {
                uiState.isLoading && uiState.movies.isEmpty() -> {
                    LoadingContent()
                }

                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error,
                        onRetry = onRetry
                    )
                }

                uiState.activeFilterLabel != null && uiState.displayedMovies.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No movies match this filter",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.displayedMovies,
                            key = { it.id }
                        ) { movie ->
                            MovieItem(
                                movie = movie,
                                isFavorite = movie.isFavorite,
                                onClick = { onMovieClick(movie.id) },
                                onFavoriteClick = { onFavoriteClick(movie) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MoviesContentLoadingPreview() {
    CinemaTheme {
        MoviesContent(
            uiState = MoviesUiState(isLoading = true),
            onMovieClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onClearFilter = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun MoviesContentSuccessPreview() {
    CinemaTheme {
        MoviesContent(
            uiState = MoviesUiState(
                movies = listOf(
                    Movie(
                        id = 1,
                        title = "The Dark Knight",
                        overview = "Batman raises the stakes.",
                        posterUrl = null,
                        backdropUrl = null,
                        releaseDate = "2008-07-18",
                        voteAverage = 8.5,
                        voteCount = 25000,
                        popularity = 85.0,
                        isFavorite = true
                    ),
                    Movie(
                        id = 2,
                        title = "Inception",
                        overview = "A thief who steals secrets.",
                        posterUrl = null,
                        backdropUrl = null,
                        releaseDate = "2010-07-16",
                        voteAverage = 8.8,
                        voteCount = 30000,
                        popularity = 90.0
                    )
                )
            ),
            onMovieClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onClearFilter = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun MoviesContentErrorPreview() {
    CinemaTheme {
        MoviesContent(
            uiState = MoviesUiState(error = "Network error"),
            onMovieClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onClearFilter = {},
            onRetry = {}
        )
    }
}
