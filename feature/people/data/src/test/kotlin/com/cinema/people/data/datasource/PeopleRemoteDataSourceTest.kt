package com.cinema.people.data.datasource

import com.cinema.people.data.network.PeopleApiService
import com.cinema.people.data.network.model.PersonDetailResponse
import com.cinema.people.data.network.model.PersonDto
import com.cinema.people.data.network.model.TrendingPeopleResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PeopleRemoteDataSourceTest {

    private lateinit var apiService: PeopleApiService
    private lateinit var dataSource: PeopleRemoteDataSource

    @Before
    fun setup() {
        apiService = mockk()
        dataSource = PeopleRemoteDataSource(apiService)
    }

    @Test
    fun `getTrendingPeople returns people from api`() = runTest {
        val expectedPeople = listOf(createPersonDto(1), createPersonDto(2))
        val response = TrendingPeopleResponse(
            page = 1,
            results = expectedPeople,
            totalPages = 1,
            totalResults = 2
        )
        coEvery { apiService.getTrendingPeople("day") } returns response

        val result = dataSource.getTrendingPeople("day")

        assertEquals(expectedPeople, result)
        coVerify { apiService.getTrendingPeople("day") }
    }

    @Test
    fun `getTrendingPeople passes timeWindow to api`() = runTest {
        val response = TrendingPeopleResponse(
            page = 1,
            results = emptyList(),
            totalPages = 0,
            totalResults = 0
        )
        coEvery { apiService.getTrendingPeople("week") } returns response

        dataSource.getTrendingPeople("week")

        coVerify { apiService.getTrendingPeople("week") }
    }

    @Test
    fun `getPersonDetail returns detail from api`() = runTest {
        val expected = createPersonDetailResponse()
        coEvery { apiService.getPersonDetail(123) } returns expected

        val result = dataSource.getPersonDetail(123)

        assertEquals(expected, result)
        coVerify { apiService.getPersonDetail(123) }
    }

    @Test(expected = Exception::class)
    fun `getTrendingPeople throws exception when api fails`() = runTest {
        coEvery { apiService.getTrendingPeople(any()) } throws Exception("Network error")

        dataSource.getTrendingPeople("day")
    }

    @Test(expected = Exception::class)
    fun `getPersonDetail throws exception when api fails`() = runTest {
        coEvery { apiService.getPersonDetail(any()) } throws Exception("Network error")

        dataSource.getPersonDetail(123)
    }

    private fun createPersonDto(id: Int) = PersonDto(
        id = id,
        name = "Person $id",
        profilePath = "/profile.jpg",
        popularity = 50.0,
        knownForDepartment = "Acting",
        gender = 2,
        adult = false,
        knownFor = emptyList()
    )

    private fun createPersonDetailResponse() = PersonDetailResponse(
        id = 123,
        name = "John Doe",
        biography = "Biography",
        birthday = "1980-01-15",
        deathday = null,
        placeOfBirth = "Los Angeles",
        profilePath = "/profile.jpg",
        popularity = 100.0,
        knownForDepartment = "Acting",
        homepage = null,
        imdbId = null,
        alsoKnownAs = emptyList(),
        adult = false,
        gender = 2
    )
}
