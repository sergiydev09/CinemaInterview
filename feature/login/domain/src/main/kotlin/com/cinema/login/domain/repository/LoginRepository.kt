package com.cinema.login.domain.repository

import com.cinema.login.domain.model.Credentials

/**
 * Repository interface for login operations.
 * Implementations handle the actual authentication logic.
 */
interface LoginRepository {

    /**
     * Attempts to log in with the provided credentials.
     *
     * @param credentials The user's login credentials
     * @return Token on success, throws exception on failure
     */
    suspend fun login(credentials: Credentials): String
}
