package com.cinema.login.domain.repository

/**
 * Repository for managing user preferences like saved username.
 */
interface UserPreferencesRepository {
    /**
     * Saves the username securely.
     * @param username The username to save
     */
    suspend fun saveUsername(username: String)

    /**
     * Gets the saved username if available.
     * @return The saved username or null if not saved
     */
    suspend fun getSavedUsername(): String?

    /**
     * Clears the saved username.
     */
    suspend fun clearUsername()
}
