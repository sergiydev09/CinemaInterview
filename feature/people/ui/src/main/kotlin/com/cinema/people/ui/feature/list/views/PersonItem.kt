package com.cinema.people.ui.feature.list.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.CinemaAsyncImage
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.people.domain.model.KnownForItem
import com.cinema.people.domain.model.Person
import com.cinema.people.ui.R
import java.util.Locale

@Composable
fun PersonItem(
    person: Person,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CinemaAsyncImage(
                imageUrl = person.profileUrl,
                contentDescription = person.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = person.knownForDepartment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (person.knownFor.isNotEmpty()) {
                    Text(
                        text = person.knownFor.take(2).joinToString(", ") { it.title },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = String.format(Locale.US, "%.1f", person.popularity),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.remove_from_favorites else R.string.add_to_favorites
                    ),
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonItemPreview() {
    CinemaTheme {
        PersonItem(
            person = Person(
                id = 1,
                name = "Leonardo DiCaprio",
                profileUrl = null,
                popularity = 85.5,
                knownForDepartment = "Acting",
                knownFor = listOf(
                    KnownForItem(1, "Inception", "movie", null),
                    KnownForItem(2, "Titanic", "movie", null)
                )
            ),
            isFavorite = false,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PersonItemFavoritePreview() {
    CinemaTheme {
        PersonItem(
            person = Person(
                id = 1,
                name = "Leonardo DiCaprio",
                profileUrl = null,
                popularity = 85.5,
                knownForDepartment = "Acting",
                knownFor = listOf(
                    KnownForItem(1, "Inception", "movie", null),
                    KnownForItem(2, "Titanic", "movie", null)
                )
            ),
            isFavorite = true,
            onClick = {},
            onFavoriteClick = {}
        )
    }
}
