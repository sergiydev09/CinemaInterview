package com.cinema.movies.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.repository.MoviesRepository
import com.cinema.movies.domain.usecase.GetMovieDetailUseCase
import com.cinema.movies.ui.ai.MoviesAIIntentHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    private lateinit var getMovieDetailUseCase: GetMovieDetailUseCase
    private lateinit var moviesRepository: MoviesRepository
    private lateinit var moviesAIIntentHandler: MoviesAIIntentHandler
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getMovieDetailUseCase = mockk()
        moviesRepository = mockk(relaxed = true)
        moviesAIIntentHandler = mockk(relaxed = true) {
            every { intents } returns emptyFlow()
        }
        savedStateHandle = SavedStateHandle(mapOf(MovieDetailViewModel.ARG_MOVIE_ID to 123))
        every { moviesRepository.isMovieFavorite(any()) } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        every { getMovieDetailUseCase(123) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.movie)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovieDetail emits loading state`() = runTest {
        every { getMovieDetailUseCase(123) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovieDetail emits success state with movie`() = runTest {
        val movieDetail = createMovieDetail()
        every { getMovieDetailUseCase(123) } returns flowOf(
            Result.Loading,
            Result.Success(movieDetail)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.movie)
            assertEquals(123, state.movie?.id)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadMovieDetail emits error state on failure`() = runTest {
        every { getMovieDetailUseCase(123) } returns flowOf(
            Result.Loading,
            Result.Error("Not found")
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals("Not found", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Retry reloads movie detail`() = runTest {
        every { getMovieDetailUseCase(123) } returns flowOf(Result.Success(createMovieDetail()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(MovieDetailIntent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { getMovieDetailUseCase(123) }
    }

    @Test
    fun `uses movieId from savedStateHandle`() = runTest {
        savedStateHandle = SavedStateHandle(mapOf(MovieDetailViewModel.ARG_MOVIE_ID to 456))
        every { getMovieDetailUseCase(456) } returns flowOf(Result.Success(createMovieDetail(456)))

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { getMovieDetailUseCase(456) }
    }

    @Test
    fun `uses default movieId 0 when not in savedStateHandle`() = runTest {
        savedStateHandle = SavedStateHandle()
        every { getMovieDetailUseCase(0) } returns flowOf(Result.Loading)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { getMovieDetailUseCase(0) }
    }

    private fun createViewModel() = MovieDetailViewModel(
        savedStateHandle,
        getMovieDetailUseCase,
        moviesRepository,
        moviesAIIntentHandler
    )

    private fun createMovieDetail(id: Int = 123) = MovieDetail(
        id = id,
        title = "Test Movie",
        overview = "Overview",
        posterUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
        backdropUrl = "https://image.tmdb.org/t/p/original/backdrop.jpg",
        releaseDate = "2024-01-15",
        voteAverage = 8.0,
        voteCount = 100,
        popularity = 50.0,
        runtime = 120,
        status = "Released",
        tagline = "Tagline",
        budget = 100_000_000L,
        revenue = 500_000_000L,
        genres = emptyList(),
        productionCompanies = emptyList()
    )
}
