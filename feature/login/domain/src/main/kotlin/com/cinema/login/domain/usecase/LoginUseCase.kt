package com.cinema.login.domain.usecase

import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.login.domain.model.Credentials
import com.cinema.login.domain.repository.LoginRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for performing login operation.
 */
class LoginUseCase @Inject constructor(
    private val loginRepository: LoginRepository
) {

    operator fun invoke(username: String, password: String): Flow<Result<String>> = flow {
        emit(loginRepository.login(Credentials(username, password)))
    }.asResult()
}
