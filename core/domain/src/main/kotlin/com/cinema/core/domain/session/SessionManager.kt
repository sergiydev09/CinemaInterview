package com.cinema.core.domain.session

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
     * Sets the callback for when session expires.
     * @param callback The callback to be notified when session expires, or null to clear
     */
    fun setOnSessionExpired(callback: (() -> Unit)?)

    /**
     * Resets the inactivity timer. Call this when user interacts with the app.
     */
    fun resetInactivityTimer()
}
