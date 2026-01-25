package com.cinema.home.ui.feature

import app.cash.turbine.test
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var favoritesRepository: FavoritesRepository
    private val testDispatcher = StandardTestDispatcher()

    private val favoriteMoviesFlow = MutableStateFlow<List<FavoriteMovie>>(emptyList())
    private val favoritePeopleFlow = MutableStateFlow<List<FavoritePerson>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        favoritesRepository = mockk()
        every { favoritesRepository.favoriteMovies } returns favoriteMoviesFlow
        every { favoritesRepository.favoritePeople } returns favoritePeopleFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty favorites`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.favoriteMovies.isEmpty())
            assertTrue(state.favoritePeople.isEmpty())
            assertFalse(state.hasFavorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits favorite movies from repository`() = runTest {
        val movies = listOf(
            createFavoriteMovie(1, "Movie 1"),
            createFavoriteMovie(2, "Movie 2")
        )
        favoriteMoviesFlow.value = movies

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip initial empty state if present
            var state = awaitItem()
            if (state.favoriteMovies.isEmpty()) {
                state = awaitItem()
            }
            assertEquals(2, state.favoriteMovies.size)
            assertEquals("Movie 1", state.favoriteMovies[0].title)
            assertEquals("Movie 2", state.favoriteMovies[1].title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits favorite people from repository`() = runTest {
        val people = listOf(
            createFavoritePerson(1, "Person 1"),
            createFavoritePerson(2, "Person 2")
        )
        favoritePeopleFlow.value = people

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip initial empty state if present
            var state = awaitItem()
            if (state.favoritePeople.isEmpty()) {
                state = awaitItem()
            }
            assertEquals(2, state.favoritePeople.size)
            assertEquals("Person 1", state.favoritePeople[0].name)
            assertEquals("Person 2", state.favoritePeople[1].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasFavorites returns true when movies exist`() = runTest {
        favoriteMoviesFlow.value = listOf(createFavoriteMovie(1, "Movie"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip initial empty state if present
            var state = awaitItem()
            if (!state.hasFavorites) {
                state = awaitItem()
            }
            assertTrue(state.hasFavorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasFavorites returns true when people exist`() = runTest {
        favoritePeopleFlow.value = listOf(createFavoritePerson(1, "Person"))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip initial empty state if present
            var state = awaitItem()
            if (!state.hasFavorites) {
                state = awaitItem()
            }
            assertTrue(state.hasFavorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `hasFavorites returns false when no favorites`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertFalse(awaitItem().hasFavorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState updates when favorites change`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            // Initial state
            assertFalse(awaitItem().hasFavorites)

            // Add a movie
            favoriteMoviesFlow.value = listOf(createFavoriteMovie(1, "Movie"))
            testDispatcher.scheduler.advanceUntilIdle()

            assertTrue(awaitItem().hasFavorites)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel() = HomeViewModel(favoritesRepository)

    private fun createFavoriteMovie(id: Int, title: String) = FavoriteMovie(
        id = id,
        title = title,
        posterUrl = "https://image.tmdb.org/t/p/w500/poster$id.jpg",
        releaseDate = "2024-01-01"
    )

    private fun createFavoritePerson(id: Int, name: String) = FavoritePerson(
        id = id,
        name = name,
        profileUrl = "https://image.tmdb.org/t/p/w185/profile$id.jpg"
    )
}
