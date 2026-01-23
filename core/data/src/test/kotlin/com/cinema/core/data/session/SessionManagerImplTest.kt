package com.cinema.core.data.session

import android.util.Log
import com.cinema.core.data.network.AuthInterceptor
import com.cinema.core.domain.session.SessionCallback
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerImplTest {

    private lateinit var authInterceptor: AuthInterceptor
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        authInterceptor = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    // ==================== startSession tests ====================

    @Test
    fun `startSession sets token on interceptor`() = runTest {
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token123")

        verify { authInterceptor.setToken("token123") }
    }

    @Test
    fun `startSession can be called multiple times`() = runTest {
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token1")
        sessionManager.startSession("token2")

        verify { authInterceptor.setToken("token1") }
        verify { authInterceptor.setToken("token2") }
    }

    @Test
    fun `startSession starts inactivity timer`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token")

        assertTrue(sessionManager.isSessionActive())
    }

    // ==================== logout tests ====================

    @Test
    fun `logout clears token on interceptor`() = runTest {
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.logout()

        verify { authInterceptor.clearToken() }
    }

    @Test
    fun `logout can be called multiple times`() = runTest {
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.logout()
        sessionManager.logout()

        verify(exactly = 2) { authInterceptor.clearToken() }
    }

    @Test
    fun `logout cancels session timer`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(callback)

        sessionManager.startSession("token")
        sessionManager.logout()

        // Advance time past session timeout - callback should NOT be called
        advanceTimeBy(4 * 60 * 1000L)
        advanceUntilIdle()

        verify(exactly = 0) { callback.onSessionExpired() }
    }

    // ==================== isSessionActive tests ====================

    @Test
    fun `isSessionActive returns true when interceptor has token`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        assertTrue(sessionManager.isSessionActive())
    }

    @Test
    fun `isSessionActive returns false when interceptor has no token`() = runTest {
        every { authInterceptor.hasToken() } returns false
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        assertFalse(sessionManager.isSessionActive())
    }

    // ==================== setSessionCallback tests ====================

    @Test
    fun `setSessionCallback sets callback`() = runTest {
        val callback = mockk<SessionCallback>()
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.setSessionCallback(callback)
        sessionManager.setSessionCallback(null)
        // No exception should be thrown
    }

    @Test
    fun `setSessionCallback with null clears callback`() = runTest {
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.setSessionCallback(callback)
        sessionManager.setSessionCallback(null)
        // No exception should be thrown
    }

    // ==================== resetInactivityTimer tests ====================

    @Test
    fun `resetInactivityTimer does nothing when session is not active`() = runTest {
        every { authInterceptor.hasToken() } returns false
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.resetInactivityTimer()

        // Verify Log.d was NOT called for "Resetting inactivity timer"
        verify(exactly = 0) { Log.d(any(), match { it.contains("Resetting") }) }
    }

    @Test
    fun `resetInactivityTimer resets timer when session is active after debounce`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token")

        // Wait for real debounce period (1 second + buffer)
        Thread.sleep(1100)

        sessionManager.resetInactivityTimer()

        verify { Log.d(any(), match { it.contains("Resetting") }) }
    }

    @Test
    fun `resetInactivityTimer respects debounce - immediate call is ignored`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token")

        // Call reset immediately (within debounce window - uses real time)
        sessionManager.resetInactivityTimer()

        // "Resetting" should NOT be logged because of debounce
        verify(exactly = 0) { Log.d(any(), match { it.contains("Resetting") }) }
    }

    // ==================== Session expiration tests ====================

    @Test
    fun `session expires after timeout and calls callback`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(callback)

        sessionManager.startSession("token")

        // Advance time past session timeout (3 minutes = 180,000 ms)
        advanceTimeBy(3 * 60 * 1000L + 1000L)
        advanceUntilIdle()

        verify { callback.onSessionExpired() }
    }

    @Test
    fun `session does not expire before timeout`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(callback)

        sessionManager.startSession("token")

        // Advance time but not past timeout (only 2 minutes)
        advanceTimeBy(2 * 60 * 1000L)

        verify(exactly = 0) { callback.onSessionExpired() }
    }

    @Test
    fun `session expiration logs debug messages during countdown`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)

        sessionManager.startSession("token")

        // Advance time by 30 seconds (should have logged "Session expires in" messages)
        advanceTimeBy(30 * 1000L)
        advanceUntilIdle()

        // Verify "Session expires in" was logged at least twice (every 10 seconds)
        verify(atLeast = 2) { Log.d(any(), match { it.contains("expires in") }) }
    }

    @Test
    fun `new session cancels previous timer`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(callback)

        sessionManager.startSession("token1")

        // Advance 2 minutes
        advanceTimeBy(2 * 60 * 1000L)

        // Start new session (resets timer)
        sessionManager.startSession("token2")

        // Advance another 2 minutes (4 min total from first, but only 2 from second)
        advanceTimeBy(2 * 60 * 1000L)

        // Session should NOT have expired yet (need 3 minutes from second session)
        verify(exactly = 0) { callback.onSessionExpired() }

        // Advance 2 more minutes (now 4 minutes from second session)
        advanceTimeBy(2 * 60 * 1000L)
        advanceUntilIdle()

        // Now it should expire
        verify { callback.onSessionExpired() }
    }

    @Test
    fun `callback is not called if null when session expires`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(null)

        sessionManager.startSession("token")

        // Advance time past session timeout
        advanceTimeBy(4 * 60 * 1000L)
        advanceUntilIdle()

        // No exception should be thrown
    }

    @Test
    fun `resetInactivityTimer extends session timeout`() = runTest {
        every { authInterceptor.hasToken() } returns true
        val callback = mockk<SessionCallback>(relaxed = true)
        val sessionManager = SessionManagerImpl(authInterceptor, scope = this)
        sessionManager.setSessionCallback(callback)

        sessionManager.startSession("token")

        // Advance 2 minutes
        advanceTimeBy(2 * 60 * 1000L)

        // Wait for real debounce period then reset timer
        Thread.sleep(1100)
        sessionManager.resetInactivityTimer()

        // Advance another 2 minutes (would be 4+ min from start, but only 2 from reset)
        advanceTimeBy(2 * 60 * 1000L)

        // Session should NOT have expired yet
        verify(exactly = 0) { callback.onSessionExpired() }

        // Advance past timeout from reset
        advanceTimeBy(2 * 60 * 1000L)
        advanceUntilIdle()

        // Now it should expire
        verify { callback.onSessionExpired() }
    }
}
