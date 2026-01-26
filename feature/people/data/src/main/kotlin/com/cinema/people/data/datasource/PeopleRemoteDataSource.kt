package com.cinema.people.data.datasource

import com.cinema.people.data.network.PeopleApiService
import com.cinema.people.data.network.model.PersonDTO
import com.cinema.people.data.network.model.PersonDetailDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRemoteDataSource @Inject constructor(
    private val apiService: PeopleApiService
) {
    suspend fun getTrendingPeople(timeWindow: String): List<PersonDTO> {
        return apiService.getTrendingPeople(timeWindow).results
    }

    suspend fun getPersonDetail(personId: Int): PersonDetailDTO {
        return apiService.getPersonDetail(personId)
    }
}
