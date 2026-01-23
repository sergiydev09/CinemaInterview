package com.cinema.people.data.repository

import com.cinema.people.data.datasource.PeopleRemoteDataSource
import com.cinema.people.data.mapper.PersonMapper.toDomain
import com.cinema.people.data.mapper.PersonMapper.toDomainList
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.model.PersonDetail
import com.cinema.people.domain.repository.PeopleRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepositoryImpl @Inject constructor(
    private val remoteDataSource: PeopleRemoteDataSource
) : PeopleRepository {

    override suspend fun getTrendingPeople(timeWindow: String): List<Person> {
        return remoteDataSource.getTrendingPeople(timeWindow).toDomainList()
    }

    override suspend fun getPersonDetail(personId: Int): PersonDetail {
        return remoteDataSource.getPersonDetail(personId).toDomain()
    }
}
