package com.cinema.movies.ui.feature.list.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.CinemaAsyncImage
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.ui.R
import java.util.Locale

@Composable
fun MovieItem(
    movie: Movie,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            Box {
                CinemaAsyncImage(
                    imageUrl = movie.posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                )
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                        ),
                        tint = if (isFavorite) Color.Red else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = movie.releaseDate.take(4),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f", movie.voteAverage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieItemPreview() {
    CinemaTheme {
        MovieItem(
            movie = Movie(
                id = 1,
                title = "The Dark Knight",
                overview = "Batman raises the stakes in his war on crime.",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.0
            ),
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MovieItemFavoritePreview() {
    CinemaTheme {
        MovieItem(
            movie = Movie(
                id = 1,
                title = "The Dark Knight",
                overview = "Batman raises the stakes in his war on crime.",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.0
            ),
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}
