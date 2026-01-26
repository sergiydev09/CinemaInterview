package com.cinema.people.data.network

import com.cinema.people.data.network.model.PersonDetailDTO
import com.cinema.people.data.network.model.TrendingPeopleDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PeopleApiService {

    @GET("trending/person/{time_window}")
    suspend fun getTrendingPeople(
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TrendingPeopleDTO

    @GET("person/{person_id}")
    suspend fun getPersonDetail(
        @Path("person_id") personId: Int,
        @Query("language") language: String = "en-US"
    ): PersonDetailDTO
}
