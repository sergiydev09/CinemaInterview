package com.cinema.people.domain.usecase

import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for getting trending people.
 */
class GetTrendingPeopleUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository
) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Person>>> = flow {
        emit(peopleRepository.getTrendingPeople(timeWindow.value))
    }.asResult()
}
