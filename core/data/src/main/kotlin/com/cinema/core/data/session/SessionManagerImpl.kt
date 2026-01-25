package com.cinema.core.data.session

import android.util.Log
import com.cinema.core.data.BuildConfig
import com.cinema.core.data.di.SessionScope
import com.cinema.core.data.network.AuthInterceptor
import com.cinema.core.domain.session.SessionCallback
import com.cinema.core.domain.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SessionManager that handles authentication state
 * and session timeout.
 *
 * @param authInterceptor Interceptor for managing auth tokens
 * @param scope CoroutineScope for session timer. In tests, inject a TestScope for virtual time control.
 */
@Singleton
class SessionManagerImpl @Inject constructor(
    private val authInterceptor: AuthInterceptor,
    @SessionScope private val scope: CoroutineScope
) : SessionManager {

    companion object {
        private const val TAG = "SessionManager"
        private const val SESSION_TIMEOUT_MS = 3 * 60 * 1000L
        private const val LOG_INTERVAL_MS = 10 * 1000L
        private const val DEBOUNCE_MS = 1000L

        private fun logDebug(message: String) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, message)
            }
        }
    }

    private var sessionCallback: SessionCallback? = null
    private var sessionJob: Job? = null
    private var lastResetTime = 0L

    override fun startSession(token: String) {
        logDebug("Starting session")
        authInterceptor.setToken(token)
        lastResetTime = System.currentTimeMillis()
        startSessionTimer()
    }

    override fun logout() {
        logDebug("Logging out")
        cancelSessionTimer()
        authInterceptor.clearToken()
    }

    override fun isSessionActive(): Boolean {
        return authInterceptor.hasToken()
    }

    override fun setSessionCallback(callback: SessionCallback?) {
        sessionCallback = callback
    }

    override fun resetInactivityTimer() {
        val now = System.currentTimeMillis()
        if (isSessionActive() && now - lastResetTime > DEBOUNCE_MS) {
            lastResetTime = now
            logDebug("Resetting inactivity timer")
            startSessionTimer()
        }
    }

    private fun startSessionTimer() {
        cancelSessionTimer()
        val totalSeconds = SESSION_TIMEOUT_MS / 1000
        logDebug("Starting inactivity timer: ${totalSeconds}s")
        sessionJob = scope.launch {
            var remainingMs = SESSION_TIMEOUT_MS
            while (remainingMs > 0) {
                val waitTime = minOf(LOG_INTERVAL_MS, remainingMs)
                delay(waitTime)
                remainingMs -= waitTime
                if (remainingMs > 0) {
                    logDebug("Session expires in: ${remainingMs / 1000}s")
                }
            }
            logDebug("Session expired due to inactivity")
            withContext(Dispatchers.Main) {
                sessionCallback?.onSessionExpired()
            }
        }
    }

    private fun cancelSessionTimer() {
        sessionJob?.cancel()
        sessionJob = null
    }
}
