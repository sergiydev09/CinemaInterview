package com.cinema.people.data.repository

import com.cinema.people.data.datasource.PeopleRemoteDataSource
import com.cinema.people.data.network.model.PersonDTO
import com.cinema.people.data.network.model.PersonDetailDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PeopleRepositoryImplTest {

    private lateinit var remoteDataSource: PeopleRemoteDataSource
    private lateinit var repository: PeopleRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = mockk()
        repository = PeopleRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getTrendingPeople returns mapped people from data source`() = runTest {
        val personDtos = listOf(createPersonDto(1), createPersonDto(2))
        coEvery { remoteDataSource.getTrendingPeople("day") } returns personDtos

        val result = repository.getTrendingPeople("day")

        assertEquals(2, result.size)
        coVerify { remoteDataSource.getTrendingPeople("day") }
    }

    @Test
    fun `getTrendingPeople maps dto to domain correctly`() = runTest {
        val personDto = createPersonDto(1, "John Doe")
        coEvery { remoteDataSource.getTrendingPeople("day") } returns listOf(personDto)

        val result = repository.getTrendingPeople("day")

        val person = result.first()
        assertEquals(1, person.id)
        assertEquals("John Doe", person.name)
    }

    @Test
    fun `getPersonDetail returns mapped person detail from data source`() = runTest {
        val detailResponse = createPersonDetailResponse()
        coEvery { remoteDataSource.getPersonDetail(123) } returns detailResponse

        val result = repository.getPersonDetail(123)

        assertEquals(123, result.id)
        assertEquals("John Doe", result.name)
        coVerify { remoteDataSource.getPersonDetail(123) }
    }

    @Test(expected = Exception::class)
    fun `getTrendingPeople throws exception from data source`() = runTest {
        coEvery { remoteDataSource.getTrendingPeople("day") } throws Exception("Network error")

        repository.getTrendingPeople("day")
    }

    @Test(expected = Exception::class)
    fun `getPersonDetail throws exception from data source`() = runTest {
        coEvery { remoteDataSource.getPersonDetail(123) } throws Exception("Not found")

        repository.getPersonDetail(123)
    }

    private fun createPersonDto(id: Int, name: String = "Person $id") = PersonDTO(
        id = id,
        name = name,
        profilePath = "/profile.jpg",
        popularity = 50.0,
        knownForDepartment = "Acting",
        gender = 2,
        adult = false,
        knownFor = emptyList()
    )

    private fun createPersonDetailResponse() = PersonDetailDTO(
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
