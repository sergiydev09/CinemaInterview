package com.cinema.login.domain.usecase

import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.login.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetSavedUsernameUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    operator fun invoke(): Flow<Result<String?>> = flow {
        emit(userPreferencesRepository.getSavedUsername())
    }.asResult()
}
