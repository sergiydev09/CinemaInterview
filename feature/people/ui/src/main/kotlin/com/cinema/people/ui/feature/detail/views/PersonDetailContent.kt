package com.cinema.people.ui.feature.detail.views

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cinema.core.ui.compose.CinemaAsyncImage
import com.cinema.core.ui.compose.CollapsingHeaderLayout
import com.cinema.core.ui.compose.FloatingActionButton
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.ui.R
import java.util.Locale

@Composable
fun PersonDetailContent(
    person: PersonDetail,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CollapsingHeaderLayout(
        onBackClick = onBackClick,
        expandedHeaderHeight = 280.dp,
        header = { progress ->
            PersonHeader(
                person = person,
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = stringResource(R.string.popularity_label),
                        value = String.format(Locale.US, "%.1f", person.popularity)
                    )
                    person.birthday?.let { birthday ->
                        StatItem(
                            label = stringResource(R.string.born_label),
                            value = birthday.take(4)
                        )
                    }
                    person.deathday?.let { deathday ->
                        StatItem(
                            label = stringResource(R.string.died_label),
                            value = deathday.take(4)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.biography_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = person.biography.ifEmpty { stringResource(R.string.no_biography) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.personal_info_label),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                person.birthday?.let { birthday ->
                    DetailRow(
                        label = stringResource(R.string.birthday_label),
                        value = birthday
                    )
                }

                person.placeOfBirth?.let { place ->
                    DetailRow(
                        label = stringResource(R.string.place_of_birth_label),
                        value = place
                    )
                }

                person.deathday?.let { deathday ->
                    DetailRow(
                        label = stringResource(R.string.deathday_label),
                        value = deathday
                    )
                }

                if (person.alsoKnownAs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.also_known_as_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = person.alsoKnownAs.joinToString(", "),
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
private fun PersonHeader(
    person: PersonDetail,
    contentAlpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                shadowElevation = 8.dp,
                modifier = Modifier.size(140.dp)
            ) {
                CinemaAsyncImage(
                    imageUrl = person.profileUrl,
                    contentDescription = person.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = person.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            DepartmentChip(department = person.knownForDepartment)
        }
    }
}

@Composable
private fun DepartmentChip(department: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Text(
            text = department,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun PersonDetailContentPreview() {
    CinemaTheme {
        PersonDetailContent(
            person = PersonDetail(
                id = 1,
                name = "Leonardo DiCaprio",
                biography = "Leonardo Wilhelm DiCaprio is an American actor and film producer. Known for his work in biopics and period films, he is the recipient of numerous accolades, including an Academy Award and three Golden Globe Awards.",
                birthday = "1974-11-11",
                deathday = null,
                placeOfBirth = "Los Angeles, California, USA",
                profileUrl = null,
                popularity = 85.5,
                knownForDepartment = "Acting",
                homepage = null,
                alsoKnownAs = listOf("Leo", "Leo DiCaprio")
            ),
            isFavorite = false,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun PersonDetailContentFavoritePreview() {
    CinemaTheme {
        PersonDetailContent(
            person = PersonDetail(
                id = 1,
                name = "Leonardo DiCaprio",
                biography = "Leonardo Wilhelm DiCaprio is an American actor and film producer. Known for his work in biopics and period films, he is the recipient of numerous accolades, including an Academy Award and three Golden Globe Awards.",
                birthday = "1974-11-11",
                deathday = null,
                placeOfBirth = "Los Angeles, California, USA",
                profileUrl = null,
                popularity = 85.5,
                knownForDepartment = "Acting",
                homepage = null,
                alsoKnownAs = listOf("Leo", "Leo DiCaprio")
            ),
            isFavorite = true,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun PersonDetailContentDeceasedPreview() {
    CinemaTheme {
        PersonDetailContent(
            person = PersonDetail(
                id = 2,
                name = "Robin Williams",
                biography = "Robin McLaurin Williams was an American actor and comedian.",
                birthday = "1951-07-21",
                deathday = "2014-08-11",
                placeOfBirth = "Chicago, Illinois, USA",
                profileUrl = null,
                popularity = 70.2,
                knownForDepartment = "Acting",
                homepage = null,
                alsoKnownAs = emptyList()
            ),
            isFavorite = false,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}

@Preview
@Composable
private fun PersonDetailContentMinimalPreview() {
    CinemaTheme {
        PersonDetailContent(
            person = PersonDetail(
                id = 3,
                name = "Unknown Actor",
                biography = "",
                birthday = null,
                deathday = null,
                placeOfBirth = null,
                profileUrl = null,
                popularity = 10.0,
                knownForDepartment = "Acting",
                homepage = null,
                alsoKnownAs = emptyList()
            ),
            isFavorite = false,
            onFavoriteClick = {},
            onBackClick = {}
        )
    }
}
