package com.cinema.people.domain.usecase

import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.repository.PeopleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for getting trending people with favorite status.
 */
class GetTrendingPeopleUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Person>>> =
        combine(
            flow { emit(peopleRepository.getTrendingPeople(timeWindow.value)) },
            favoritesRepository.favoritePeople
        ) { people, favoritesMap ->
            people.map { person -> person.copy(isFavorite = favoritesMap.containsKey(person.id)) }
        }.asResult()
}
