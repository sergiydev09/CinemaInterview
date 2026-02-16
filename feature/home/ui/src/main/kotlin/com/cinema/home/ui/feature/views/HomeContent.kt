package com.cinema.home.ui.feature.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.CinemaAsyncImage
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.home.domain.model.FavoriteMovie
import com.cinema.home.domain.model.FavoritePerson
import com.cinema.home.ui.R
import com.cinema.home.ui.feature.HomeUiState

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.hasFavorites) {
        FavoritesContent(
            uiState = uiState,
            onMovieClick = onMovieClick,
            onPersonClick = onPersonClick,
            modifier = modifier
        )
    } else {
        EmptyFavoritesContent(modifier = modifier)
    }
}

@Composable
private fun FavoritesContent(
    uiState: HomeUiState,
    onMovieClick: (Int) -> Unit,
    onPersonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.your_favorites),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        if (uiState.favoriteMovies.isNotEmpty()) {
            Text(
                text = stringResource(R.string.favorite_movies),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.favoriteMovies,
                    key = { it.id }
                ) { movie ->
                    FavoriteMovieItem(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (uiState.favoritePeople.isNotEmpty()) {
            Text(
                text = stringResource(R.string.favorite_people),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = uiState.favoritePeople,
                    key = { it.id }
                ) { person ->
                    FavoritePersonItem(
                        person = person,
                        onClick = { onPersonClick(person.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FavoriteMovieItem(
    movie: FavoriteMovie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            CinemaAsyncImage(
                imageUrl = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = movie.releaseDate?.take(4).orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FavoritePersonItem(
    person: FavoritePerson,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CinemaAsyncImage(
            imageUrl = person.profileUrl,
            contentDescription = person.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = person.name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyFavoritesContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.no_favorites_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.no_favorites_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun HomeContentEmptyPreview() {
    CinemaTheme {
        HomeContent(
            uiState = HomeUiState(),
            onMovieClick = {},
            onPersonClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeContentWithFavoritesPreview() {
    CinemaTheme {
        HomeContent(
            uiState = HomeUiState(
                favoriteMovies = listOf(
                    FavoriteMovie(
                        id = 1,
                        title = "The Dark Knight",
                        posterUrl = null,
                        releaseDate = "2008-07-18"
                    ),
                    FavoriteMovie(
                        id = 2,
                        title = "Inception",
                        posterUrl = null,
                        releaseDate = "2010-07-16"
                    )
                ),
                favoritePeople = listOf(
                    FavoritePerson(
                        id = 1,
                        name = "Leonardo DiCaprio",
                        profileUrl = null
                    )
                )
            ),
            onMovieClick = {},
            onPersonClick = {}
        )
    }
}
