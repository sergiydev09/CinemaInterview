package com.cinema.core.domain.session

/**
 * Callback interface for session-related events.
 */
interface SessionCallback {
    /**
     * Called when the session has expired.
     */
    fun onSessionExpired()
}

/**
 * Interface for managing user session and authentication state.
 */
interface SessionManager {
    /**
     * Starts a new session with the provided token.
     * @param token The authentication token
     */
    fun startSession(token: String)

    /**
     * Ends the current session and clears the token.
     */
    fun logout()

    /**
     * Checks if there is an active session.
     * @return true if a session is active
     */
    fun isSessionActive(): Boolean

    /**
     * Sets the callback for session events.
     * @param callback The callback to be notified of session events
     */
    fun setSessionCallback(callback: SessionCallback?)

    /**
     * Resets the inactivity timer. Call this when user interacts with the app.
     */
    fun resetInactivityTimer()
}
