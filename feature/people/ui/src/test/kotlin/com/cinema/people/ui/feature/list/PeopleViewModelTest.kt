package com.cinema.people.ui.feature.list

import app.cash.turbine.test
import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import com.cinema.people.domain.usecase.GetTrendingPeopleUseCase
import com.cinema.people.ui.ai.PeopleAIIntentHandler
import io.mockk.coVerify
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PeopleViewModelTest {

    private lateinit var getTrendingPeopleUseCase: GetTrendingPeopleUseCase
    private lateinit var peopleRepository: PeopleRepository
    private lateinit var peopleAIIntentHandler: PeopleAIIntentHandler
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getTrendingPeopleUseCase = mockk()
        peopleRepository = mockk(relaxed = true)
        peopleAIIntentHandler = mockk(relaxed = true) {
            every { intents } returns emptyFlow()
        }
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
    fun `ChangeTimeWindow updates state and reloads`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))
        every { getTrendingPeopleUseCase(TimeWindow.WEEK) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(PeopleIntent.ChangeTimeWindow(TimeWindow.WEEK))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(TimeWindow.WEEK, state.selectedTimeWindow)
            cancelAndIgnoreRemainingEvents()
        }

        verify { getTrendingPeopleUseCase(TimeWindow.WEEK) }
    }

    @Test
    fun `ChangeTimeWindow does nothing if same timeWindow`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(PeopleIntent.ChangeTimeWindow(TimeWindow.DAY))
        testDispatcher.scheduler.advanceUntilIdle()

        // Should only be called once (from init)
        verify(exactly = 1) { getTrendingPeopleUseCase(TimeWindow.DAY) }
    }

    @Test
    fun `Retry reloads people`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(PeopleIntent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { getTrendingPeopleUseCase(TimeWindow.DAY) }
    }

    @Test
    fun `ToggleFavorite calls use case`() = runTest {
        every { getTrendingPeopleUseCase(TimeWindow.DAY) } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val person = createPerson(1)
        viewModel.handleIntent(PeopleIntent.ToggleFavorite(person))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { peopleRepository.toggleFavoritePerson(any(), any(), any()) }
    }

    private fun createViewModel() = PeopleViewModel(
        getTrendingPeopleUseCase,
        peopleRepository,
        peopleAIIntentHandler
    )

    private fun createPerson(id: Int) = Person(
        id = id,
        name = "Person $id",
        profileUrl = "https://image.tmdb.org/t/p/w185/profile.jpg",
        popularity = 50.0,
        knownForDepartment = "Acting",
        knownFor = emptyList()
    )
}
