package com.cinema.people.domain.repository

import com.cinema.people.domain.model.Person
import com.cinema.people.domain.model.PersonDetail
import kotlinx.coroutines.flow.Flow

interface PeopleRepository {
    fun getTrendingPeople(timeWindow: String): Flow<List<Person>>
    suspend fun getPersonDetail(personId: Int): PersonDetail
    fun isPersonFavorite(personId: Int): Flow<Boolean>
    suspend fun toggleFavoritePerson(id: Int, name: String, profileUrl: String?)
}
