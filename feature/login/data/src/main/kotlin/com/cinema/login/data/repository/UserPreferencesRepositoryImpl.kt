package com.cinema.login.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.data.datasource.get
import com.cinema.core.data.datasource.save
import com.cinema.login.domain.repository.UserPreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserPreferencesRepository.
 * Uses SecureLocalDataSource for encrypted storage.
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val secureLocalDataSource: SecureLocalDataSource
) : UserPreferencesRepository {

    companion object {
        private const val KEY_USERNAME = "saved_username"
    }

    override suspend fun saveUsername(username: String) {
        secureLocalDataSource.save(KEY_USERNAME, username)
    }

    override suspend fun getSavedUsername(): String? {
        return secureLocalDataSource.get(KEY_USERNAME)
    }

    override suspend fun clearUsername() {
        secureLocalDataSource.remove(KEY_USERNAME)
    }
}
