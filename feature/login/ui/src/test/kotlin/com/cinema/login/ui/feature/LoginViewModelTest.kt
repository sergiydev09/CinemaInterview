package com.cinema.login.ui.feature

import app.cash.turbine.test
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.domain.repository.UserPreferencesRepository
import com.cinema.login.domain.usecase.LoginUseCase
import com.cinema.login.domain.usecase.ValidateCredentialsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var loginUseCase: LoginUseCase
    private lateinit var validateCredentialsUseCase: ValidateCredentialsUseCase
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var sessionManager: SessionManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        validateCredentialsUseCase = mockk()
        userPreferencesRepository = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)

        coEvery { userPreferencesRepository.getSavedUsername() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.username)
            assertEquals("", state.password)
            assertFalse(state.rememberUsername)
            assertFalse(state.isLoading)
            assertTrue(state.usernameErrors.isEmpty())
            assertTrue(state.passwordErrors.isEmpty())
            assertFalse(state.showInvalidCredentialsError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads saved username on init`() = runTest {
        coEvery { userPreferencesRepository.getSavedUsername() } returns "savedUser"

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("savedUser", state.username)
            assertTrue(state.rememberUsername)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `UsernameChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(LoginIntent.UsernameChanged("newUser"))

        viewModel.uiState.test {
            assertEquals("newUser", awaitItem().username)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `PasswordChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(LoginIntent.PasswordChanged("newPass"))

        viewModel.uiState.test {
            assertEquals("newPass", awaitItem().password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `RememberUsernameChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.handleIntent(LoginIntent.RememberUsernameChanged(true))

        viewModel.uiState.test {
            assertTrue(awaitItem().rememberUsername)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LoginClicked with validation errors updates state`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns listOf(
            ValidationErrorType.USERNAME_TOO_SHORT,
            ValidationErrorType.PASSWORD_TOO_SHORT
        )

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("ab"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("short"))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.usernameErrors.contains(ValidationErrorType.USERNAME_TOO_SHORT))
            assertTrue(state.passwordErrors.contains(ValidationErrorType.PASSWORD_TOO_SHORT))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LoginClicked with valid credentials calls loginUseCase`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase("user", "Password1!") } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { loginUseCase("user", "Password1!") }
    }

    @Test
    fun `LoginClicked shows loading state`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful login saves username when remember is checked`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token"))
        coEvery { userPreferencesRepository.saveUsername("user") } just runs
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.RememberUsernameChanged(true))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { userPreferencesRepository.saveUsername("user") }
    }

    @Test
    fun `successful login clears username when remember is not checked`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token"))
        coEvery { userPreferencesRepository.clearUsername() } just runs
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.RememberUsernameChanged(false))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { userPreferencesRepository.clearUsername() }
    }

    @Test
    fun `successful login starts session`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token123"))
        coEvery { userPreferencesRepository.clearUsername() } just runs
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        verify { sessionManager.startSession("token123") }
    }

    @Test
    fun `successful login emits NavigateToHome event`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token"))
        coEvery { userPreferencesRepository.clearUsername() } just runs
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))

        viewModel.events.test {
            viewModel.handleIntent(LoginIntent.LoginClicked)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(LoginEvent.NavigateToHome, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed login shows error`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Error("Invalid"))

        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("Password1!"))
        viewModel.handleIntent(LoginIntent.LoginClicked)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.showInvalidCredentialsError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login button is disabled when fields are empty`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertFalse(awaitItem().isLoginButtonEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `login button is enabled when fields are filled`() = runTest {
        val viewModel = createViewModel()
        viewModel.handleIntent(LoginIntent.UsernameChanged("user"))
        viewModel.handleIntent(LoginIntent.PasswordChanged("pass"))

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoginButtonEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel() = LoginViewModel(
        loginUseCase,
        validateCredentialsUseCase,
        userPreferencesRepository,
        sessionManager
    )
}
