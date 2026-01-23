package com.cinema.people.data.datasource

import com.cinema.people.data.network.PeopleApiService
import com.cinema.people.data.network.model.PersonDetailResponse
import com.cinema.people.data.network.model.PersonDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRemoteDataSource @Inject constructor(
    private val apiService: PeopleApiService
) {
    suspend fun getTrendingPeople(timeWindow: String): List<PersonDto> {
        return apiService.getTrendingPeople(timeWindow).results
    }

    suspend fun getPersonDetail(personId: Int): PersonDetailResponse {
        return apiService.getPersonDetail(personId)
    }
}
