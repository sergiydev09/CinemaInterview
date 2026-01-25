package com.cinema.movies.ui.feature.list

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.favorites.domain.usecase.ToggleMovieFavoriteUseCase
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.usecase.GetTrendingMoviesUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {

    private lateinit var getTrendingMoviesUseCase: GetTrendingMoviesUseCase
    private lateinit var toggleMovieFavoriteUseCase: ToggleMovieFavoriteUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTrendingMoviesUseCase = mockk()
        toggleMovieFavoriteUseCase = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        every { getTrendingMoviesUseCase(any()) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.movies.isEmpty())
            assertNull(state.error)
            assertEquals(TimeWindow.DAY, state.selectedTimeWindow)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovies emits loading state`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovies emits success state with movies`() = runTest {
        val movies = listOf(createMovie(1), createMovie(2))
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(
            Result.Loading,
            Result.Success(movies)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.movies.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovies emits error state on failure`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(
            Result.Loading,
            Result.Error("Network error")
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Network error", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onTimeWindowChanged updates state and reloads`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))
        every { getTrendingMoviesUseCase(TimeWindow.WEEK) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTimeWindowChanged(TimeWindow.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimeWindow.WEEK, state.selectedTimeWindow)
            cancelAndIgnoreRemainingEvents()
        }

        verify { getTrendingMoviesUseCase(TimeWindow.WEEK) }
    }

    @Test
    fun `onTimeWindowChanged does nothing if same timeWindow`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTimeWindowChanged(TimeWindow.DAY)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once (from init)
        verify(exactly = 1) { getTrendingMoviesUseCase(TimeWindow.DAY) }
    }

    @Test
    fun `retry reloads movies`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { getTrendingMoviesUseCase(TimeWindow.DAY) }
    }

    @Test
    fun `toggleFavorite calls use case`() = runTest {
        every { getTrendingMoviesUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val movie = createMovie(1)
        viewModel.toggleFavorite(movie)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { toggleMovieFavoriteUseCase(any()) }
    }

    private fun createViewModel() = MoviesViewModel(getTrendingMoviesUseCase, toggleMovieFavoriteUseCase)

    private fun createMovie(id: Int) = Movie(
        id = id,
        title = "Movie $id",
        overview = "Overview",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0
    )
}
