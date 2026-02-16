package com.cinema.people.domain.usecase

import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingPeopleUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository
) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Person>>> =
        peopleRepository.getTrendingPeople(timeWindow.value).asResult()
}
