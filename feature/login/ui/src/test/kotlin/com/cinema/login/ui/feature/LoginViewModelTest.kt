package com.cinema.login.ui.feature

import app.cash.turbine.test
import com.cinema.core.domain.session.SessionManager
import com.cinema.core.domain.util.Result
import com.cinema.login.domain.model.ValidationErrorType
import com.cinema.login.domain.usecase.ClearUsernameUseCase
import com.cinema.login.domain.usecase.GetSavedUsernameUseCase
import com.cinema.login.domain.usecase.LoginUseCase
import com.cinema.login.domain.usecase.SaveUsernameUseCase
import com.cinema.login.domain.usecase.ValidateCredentialsUseCase
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
    private lateinit var getSavedUsernameUseCase: GetSavedUsernameUseCase
    private lateinit var saveUsernameUseCase: SaveUsernameUseCase
    private lateinit var clearUsernameUseCase: ClearUsernameUseCase
    private lateinit var sessionManager: SessionManager

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        loginUseCase = mockk()
        validateCredentialsUseCase = mockk()
        getSavedUsernameUseCase = mockk()
        saveUsernameUseCase = mockk()
        clearUsernameUseCase = mockk()
        sessionManager = mockk(relaxed = true)

        every { getSavedUsernameUseCase() } returns flowOf(Result.Success(null))
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
        every { getSavedUsernameUseCase() } returns flowOf(Result.Success("savedUser"))

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
    fun `onUsernameChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onUsernameChanged("newUser")

        viewModel.uiState.test {
            assertEquals("newUser", awaitItem().username)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onPasswordChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onPasswordChanged("newPass")

        viewModel.uiState.test {
            assertEquals("newPass", awaitItem().password)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRememberUsernameChanged updates state`() = runTest {
        val viewModel = createViewModel()

        viewModel.onRememberUsernameChanged(true)

        viewModel.uiState.test {
            assertTrue(awaitItem().rememberUsername)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked with validation errors updates state`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns listOf(
            ValidationErrorType.USERNAME_TOO_SHORT,
            ValidationErrorType.PASSWORD_TOO_SHORT
        )

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("ab")
        viewModel.onPasswordChanged("short")
        viewModel.onLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.usernameErrors.contains(ValidationErrorType.USERNAME_TOO_SHORT))
            assertTrue(state.passwordErrors.contains(ValidationErrorType.PASSWORD_TOO_SHORT))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onLoginClicked with valid credentials calls loginUseCase`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase("user", "Password1!") } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { loginUseCase("user", "Password1!") }
    }

    @Test
    fun `onLoginClicked shows loading state`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Loading)

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onLoginClicked()
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
        every { saveUsernameUseCase("user") } returns flowOf(Result.Success(Unit))
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onRememberUsernameChanged(true)
        viewModel.onLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { saveUsernameUseCase("user") }
    }

    @Test
    fun `successful login clears username when remember is not checked`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token"))
        every { clearUsernameUseCase() } returns flowOf(Result.Success(Unit))
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onRememberUsernameChanged(false)
        viewModel.onLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { clearUsernameUseCase() }
    }

    @Test
    fun `successful login starts session`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token123"))
        every { clearUsernameUseCase() } returns flowOf(Result.Success(Unit))
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onLoginClicked()
        testDispatcher.scheduler.advanceUntilIdle()

        verify { sessionManager.startSession("token123") }
    }

    @Test
    fun `successful login emits NavigateToHome event`() = runTest {
        every { validateCredentialsUseCase(any(), any()) } returns emptyList()
        every { loginUseCase(any(), any()) } returns flowOf(Result.Success("token"))
        every { clearUsernameUseCase() } returns flowOf(Result.Success(Unit))
        every { sessionManager.startSession(any()) } just runs

        val viewModel = createViewModel()
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")

        viewModel.events.test {
            viewModel.onLoginClicked()
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
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("Password1!")
        viewModel.onLoginClicked()
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
        viewModel.onUsernameChanged("user")
        viewModel.onPasswordChanged("pass")

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoginButtonEnabled)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel() = LoginViewModel(
        loginUseCase,
        validateCredentialsUseCase,
        getSavedUsernameUseCase,
        saveUsernameUseCase,
        clearUsernameUseCase,
        sessionManager
    )
}
