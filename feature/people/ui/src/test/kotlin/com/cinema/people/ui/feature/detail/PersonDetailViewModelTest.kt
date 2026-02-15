package com.cinema.people.ui.feature.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.cinema.core.domain.util.Result
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.repository.PeopleRepository
import com.cinema.people.domain.usecase.GetPersonDetailUseCase
import com.cinema.people.ui.ai.PeopleAIIntentHandler
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
class PersonDetailViewModelTest {

    private lateinit var getPersonDetailUseCase: GetPersonDetailUseCase
    private lateinit var peopleRepository: PeopleRepository
    private lateinit var peopleAIIntentHandler: PeopleAIIntentHandler
    private lateinit var savedStateHandle: SavedStateHandle
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getPersonDetailUseCase = mockk()
        peopleRepository = mockk(relaxed = true)
        peopleAIIntentHandler = mockk(relaxed = true) {
            every { intents } returns emptyFlow()
        }
        savedStateHandle = SavedStateHandle(mapOf(PersonDetailViewModel.ARG_PERSON_ID to 123))
        every { peopleRepository.isPersonFavorite(any()) } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        every { getPersonDetailUseCase(123) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNull(state.person)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPersonDetail emits loading state`() = runTest {
        every { getPersonDetailUseCase(123) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPersonDetail emits success state with person`() = runTest {
        val personDetail = createPersonDetail()
        every { getPersonDetailUseCase(123) } returns flowOf(
            Result.Loading,
            Result.Success(personDetail)
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertNotNull(state.person)
            assertEquals(123, state.person?.id)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadPersonDetail emits error state on failure`() = runTest {
        every { getPersonDetailUseCase(123) } returns flowOf(
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
    fun `Retry reloads person detail`() = runTest {
        every { getPersonDetailUseCase(123) } returns flowOf(Result.Success(createPersonDetail()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.handleIntent(PersonDetailIntent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(exactly = 2) { getPersonDetailUseCase(123) }
    }

    @Test
    fun `uses personId from savedStateHandle`() = runTest {
        savedStateHandle = SavedStateHandle(mapOf(PersonDetailViewModel.ARG_PERSON_ID to 456))
        every { getPersonDetailUseCase(456) } returns flowOf(Result.Success(createPersonDetail(456)))

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { getPersonDetailUseCase(456) }
    }

    @Test
    fun `uses default personId 0 when not in savedStateHandle`() = runTest {
        savedStateHandle = SavedStateHandle()
        every { getPersonDetailUseCase(0) } returns flowOf(Result.Loading)

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { getPersonDetailUseCase(0) }
    }

    private fun createViewModel() = PersonDetailViewModel(
        savedStateHandle,
        getPersonDetailUseCase,
        peopleRepository,
        peopleAIIntentHandler
    )

    private fun createPersonDetail(id: Int = 123) = PersonDetail(
        id = id,
        name = "John Doe",
        biography = "Biography",
        birthday = "1980-01-15",
        deathday = null,
        placeOfBirth = "Los Angeles",
        profileUrl = "https://image.tmdb.org/t/p/w185/profile.jpg",
        popularity = 100.0,
        knownForDepartment = "Acting",
        homepage = null,
        alsoKnownAs = emptyList()
    )
}
