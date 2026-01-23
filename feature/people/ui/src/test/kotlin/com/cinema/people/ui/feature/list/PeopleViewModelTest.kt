package com.cinema.people.ui.feature.list

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.usecase.GetTrendingPeopleUseCase
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
class PeopleViewModelTest {

    private lateinit var getTrendingPeopleUseCase: GetTrendingPeopleUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTrendingPeopleUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        every { getTrendingPeopleUseCase(any()) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.people.isEmpty())
            assertNull(state.error)
            assertEquals(TimeWindow.DAY, state.selectedTimeWindow)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPeople emits loading state`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPeople emits success state with people`() = runTest {
        val people = listOf(createPerson(1), createPerson(2))
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(
            Result.Loading,
            Result.Success(people)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.people.size)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPeople emits error state on failure`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(
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
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))
        every { getTrendingPeopleUseCase(TimeWindow.WEEK) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTimeWindowChanged(TimeWindow.WEEK)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimeWindow.WEEK, state.selectedTimeWindow)
            cancelAndIgnoreRemainingEvents()
        }

        verify { getTrendingPeopleUseCase(TimeWindow.WEEK) }
    }

    @Test
    fun `onTimeWindowChanged does nothing if same timeWindow`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onTimeWindowChanged(TimeWindow.DAY)
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once (from init)
        verify(exactly = 1) { getTrendingPeopleUseCase(TimeWindow.DAY) }
    }

    @Test
    fun `retry reloads people`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.retry()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { getTrendingPeopleUseCase(TimeWindow.DAY) }
    }

    private fun createViewModel() = PeopleViewModel(getTrendingPeopleUseCase)

    private fun createPerson(id: Int) = Person(
        id = id,
        name = "Person $id",
        profileUrl = "https://image.tmdb.org/t/p/w185/profile.jpg",
        popularity = 50.0,
        knownForDepartment = "Acting",
        knownFor = emptyList()
    )
}
