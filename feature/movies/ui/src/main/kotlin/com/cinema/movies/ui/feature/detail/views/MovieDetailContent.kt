package com.cinema.movies.ui.feature.detail.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.CinemaAsyncImage
import com.cinema.core.ui.compose.CollapsingHeaderLayout
import com.cinema.core.ui.compose.FloatingActionButton
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.movies.domain.model.Genre
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.ui.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MovieDetailContent(
    movie: MovieDetail,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    CollapsingHeaderLayout(
        onBackClick = onBackClick,
        header = { progress ->
            MovieHeader(
                movie = movie,
                contentAlpha = 1f - progress
            )
        },
        actions = {
            FloatingActionButton(
                onClick = onFavoriteClick,
                icon = {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(
                            if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                        ),
                        tint = if (isFavorite) Color.Red else Color.White
                    )
                }
            )
        },
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                movie.tagline?.takeIf { it.isNotEmpty() }?.let { tagline ->
                    Text(
                        text = "\"$tagline\"",
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (movie.genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        movie.genres.forEach { genre ->
                            GenreChip(name = genre.name)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text(
                    text = stringResource(R.string.overview_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = movie.overview.ifEmpty { stringResource(R.string.no_overview) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.details_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(
                    label = stringResource(R.string.vote_count_label),
                    value = NumberFormat.getNumberInstance(Locale.US).format(movie.voteCount)
                )

                if (movie.budget > 0) {
                    DetailRow(
                        label = stringResource(R.string.budget_label),
                        value = currencyFormat.format(movie.budget)
                    )
                }

                if (movie.revenue > 0) {
                    DetailRow(
                        label = stringResource(R.string.revenue_label),
                        value = currencyFormat.format(movie.revenue)
                    )
                }

                if (movie.productionCompanies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.production_companies_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = movie.productionCompanies.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        modifier = modifier
    )
}

@Composable
private fun MovieHeader(
    movie: MovieDetail,
    contentAlpha: Float
) {
    Box(modifier = Modifier.fillMaxSize()) {
        CinemaAsyncImage(
            imageUrl = movie.backdropUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.6f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .alpha(contentAlpha)
        ) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingBadge(rating = movie.voteAverage)
                Text(
                    text = movie.releaseDate.take(4),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                movie.runtime?.let { runtime ->
                    Text(
                        text = stringResource(R.string.runtime_format, runtime),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingBadge(rating: Double) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun GenreChip(name: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview
@Composable
private fun MovieDetailContentPreview() {
    CinemaTheme {
        MovieDetailContent(
            movie = MovieDetail(
                id = 1,
                title = "The Dark Knight",
                overview = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.0,
                runtime = 152,
                status = "Released",
                tagline = "Why So Serious?",
                budget = 185000000,
                revenue = 1004558444,
                genres = listOf(
                    Genre(28, "Action"),
                    Genre(80, "Crime"),
                    Genre(18, "Drama")
                ),
                productionCompanies = listOf("Warner Bros.", "Legendary Pictures")
            ),
            isFavorite = false,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun MovieDetailContentFavoritePreview() {
    CinemaTheme {
        MovieDetailContent(
            movie = MovieDetail(
                id = 1,
                title = "The Dark Knight",
                overview = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2008-07-18",
                voteAverage = 8.5,
                voteCount = 25000,
                popularity = 85.0,
                runtime = 152,
                status = "Released",
                tagline = "Why So Serious?",
                budget = 185000000,
                revenue = 1004558444,
                genres = listOf(
                    Genre(28, "Action"),
                    Genre(80, "Crime"),
                    Genre(18, "Drama")
                ),
                productionCompanies = listOf("Warner Bros.", "Legendary Pictures")
            ),
            isFavorite = true,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun MovieDetailContentMinimalPreview() {
    CinemaTheme {
        MovieDetailContent(
            movie = MovieDetail(
                id = 2,
                title = "Inception",
                overview = "A thief who steals corporate secrets through the use of dream-sharing technology.",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2010-07-16",
                voteAverage = 8.8,
                voteCount = 30000,
                popularity = 90.0,
                runtime = null,
                status = null,
                tagline = null,
                budget = 0,
                revenue = 0,
                genres = emptyList(),
                productionCompanies = emptyList()
            ),
            isFavorite = false,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}
