package com.cinema.people.domain.repository

import com.cinema.people.domain.model.Person
import com.cinema.people.domain.model.PersonDetail

interface PeopleRepository {

    suspend fun getTrendingPeople(timeWindow: String): List<Person>

    suspend fun getPersonDetail(personId: Int): PersonDetail
}
