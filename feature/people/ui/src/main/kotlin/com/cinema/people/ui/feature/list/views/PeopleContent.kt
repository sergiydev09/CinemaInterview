package com.cinema.people.ui.feature.list.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.cinema.core.ui.compose.LoadingContent
import com.cinema.core.ui.compose.TimeWindowToggle
import com.cinema.core.ui.lazylist.rememberAnimatedLazyListState
import com.cinema.core.ui.theme.CinemaTheme
import com.cinema.people.domain.model.KnownForItem
import com.cinema.people.domain.model.Person
import com.cinema.people.ui.R
import com.cinema.people.ui.feature.list.PeopleUiState

@Composable
fun PeopleContent(
    uiState: PeopleUiState,
    onPersonClick: (Int) -> Unit,
    onFavoriteClick: (Person) -> Unit,
    onTimeWindowChanged: (TimeWindow) -> Unit,
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
                text = stringResource(R.string.trending_people),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            TimeWindowToggle(
                selectedTimeWindow = uiState.selectedTimeWindow,
                onTimeWindowSelected = onTimeWindowChanged
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val listState = rememberLazyListState()
            rememberAnimatedLazyListState(listState)

            when {
                uiState.isLoading && uiState.people.isEmpty() -> {
                    LoadingContent()
                }

                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error,
                        onRetry = onRetry
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.people,
                            key = { it.id }
                        ) { person ->
                            PersonItem(
                                person = person,
                                isFavorite = person.isFavorite,
                                onClick = { onPersonClick(person.id) },
                                onFavoriteClick = { onFavoriteClick(person) },
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
private fun PeopleContentLoadingPreview() {
    CinemaTheme {
        PeopleContent(
            uiState = PeopleUiState(isLoading = true),
            onPersonClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun PeopleContentSuccessPreview() {
    CinemaTheme {
        PeopleContent(
            uiState = PeopleUiState(
                people = listOf(
                    Person(
                        id = 1,
                        name = "Leonardo DiCaprio",
                        profileUrl = null,
                        popularity = 85.5,
                        knownForDepartment = "Acting",
                        knownFor = listOf(
                            KnownForItem(1, "Inception", "movie", null)
                        ),
                        isFavorite = true
                    ),
                    Person(
                        id = 2,
                        name = "Tom Hanks",
                        profileUrl = null,
                        popularity = 78.2,
                        knownForDepartment = "Acting",
                        knownFor = listOf(
                            KnownForItem(2, "Forrest Gump", "movie", null)
                        )
                    )
                )
            ),
            onPersonClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun PeopleContentErrorPreview() {
    CinemaTheme {
        PeopleContent(
            uiState = PeopleUiState(error = "Network error"),
            onPersonClick = {},
            onFavoriteClick = {},
            onTimeWindowChanged = {},
            onRetry = {}
        )
    }
}
