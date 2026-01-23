package com.cinema.people.domain.usecase

import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPersonDetailUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository
) {
    operator fun invoke(personId: Int): Flow<Result<PersonDetail>> = flow {
        emit(peopleRepository.getPersonDetail(personId))
    }.asResult()
}
