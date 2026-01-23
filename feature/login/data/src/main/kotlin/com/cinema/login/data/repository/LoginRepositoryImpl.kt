package com.cinema.login.data.repository

import com.cinema.login.data.BuildConfig
import com.cinema.login.domain.model.Credentials
import com.cinema.login.domain.repository.LoginRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LoginRepository.
 * For this demo, we simulate login with a delay and return the TMDB API token.
 * In a real app, this would call an authentication API.
 */
@Singleton
class LoginRepositoryImpl @Inject constructor() : LoginRepository {

    companion object {
        private const val LOGIN_DELAY_MS = 1500L
    }

    override suspend fun login(credentials: Credentials): String {
        delay(LOGIN_DELAY_MS)
        return BuildConfig.TMDB_API_TOKEN
    }
}
