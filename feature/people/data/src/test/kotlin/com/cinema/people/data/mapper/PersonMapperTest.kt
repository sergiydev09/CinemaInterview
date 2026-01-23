package com.cinema.people.data.mapper

import com.cinema.people.data.mapper.PersonMapper.toDomain
import com.cinema.people.data.mapper.PersonMapper.toDomainList
import com.cinema.people.data.network.model.KnownForDto
import com.cinema.people.data.network.model.PersonDetailResponse
import com.cinema.people.data.network.model.PersonDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonMapperTest {

    @Test
    fun `PersonDto toDomain maps all fields correctly`() {
        val dto = createPersonDto()

        val person = dto.toDomain()

        assertEquals(1, person.id)
        assertEquals("John Doe", person.name)
        assertEquals("https://image.tmdb.org/t/p/w185/profile.jpg", person.profileUrl)
        assertEquals(100.0, person.popularity, 0.01)
        assertEquals("Acting", person.knownForDepartment)
        assertEquals(2, person.knownFor.size)
    }

    @Test
    fun `PersonDto toDomain handles null profilePath`() {
        val dto = createPersonDto(profilePath = null)

        val person = dto.toDomain()

        assertNull(person.profileUrl)
    }

    @Test
    fun `PersonDto toDomain handles null knownForDepartment`() {
        val dto = createPersonDto(knownForDepartment = null)

        val person = dto.toDomain()

        assertEquals("Unknown", person.knownForDepartment)
    }

    @Test
    fun `PersonDto toDomain handles null knownFor`() {
        val dto = createPersonDto(knownFor = null)

        val person = dto.toDomain()

        assertTrue(person.knownFor.isEmpty())
    }

    @Test
    fun `List toDomainList maps all items`() {
        val dtos = listOf(
            createPersonDto(id = 1, name = "Person 1"),
            createPersonDto(id = 2, name = "Person 2")
        )

        val people = dtos.toDomainList()

        assertEquals(2, people.size)
        assertEquals("Person 1", people[0].name)
        assertEquals("Person 2", people[1].name)
    }

    @Test
    fun `empty list toDomainList returns empty list`() {
        val dtos = emptyList<PersonDto>()

        val people = dtos.toDomainList()

        assertTrue(people.isEmpty())
    }

    @Test
    fun `PersonDetailResponse toDomain maps all fields correctly`() {
        val response = createPersonDetailResponse()

        val detail = response.toDomain()

        assertEquals(1, detail.id)
        assertEquals("John Doe", detail.name)
        assertEquals("Biography text", detail.biography)
        assertEquals("1980-01-15", detail.birthday)
        assertNull(detail.deathday)
        assertEquals("Los Angeles, USA", detail.placeOfBirth)
        assertEquals(100.0, detail.popularity, 0.01)
        assertEquals("Acting", detail.knownForDepartment)
        assertEquals(2, detail.alsoKnownAs.size)
    }

    @Test
    fun `PersonDetailResponse toDomain handles null biography`() {
        val response = createPersonDetailResponse(biography = null)

        val detail = response.toDomain()

        assertEquals("", detail.biography)
    }

    @Test
    fun `PersonDetailResponse toDomain handles null alsoKnownAs`() {
        val response = createPersonDetailResponse(alsoKnownAs = null)

        val detail = response.toDomain()

        assertTrue(detail.alsoKnownAs.isEmpty())
    }

    @Test
    fun `KnownForDto toDomain uses title when available`() {
        val dto = KnownForDto(
            id = 1,
            title = "Movie Title",
            name = "TV Name",
            mediaType = "movie",
            posterPath = "/poster.jpg",
            overview = "Overview",
            voteAverage = 8.0
        )

        val item = dto.toDomain()

        assertEquals("Movie Title", item.title)
    }

    @Test
    fun `KnownForDto toDomain uses name when title is null`() {
        val dto = KnownForDto(
            id = 1,
            title = null,
            name = "TV Name",
            mediaType = "tv",
            posterPath = "/poster.jpg",
            overview = "Overview",
            voteAverage = 8.0
        )

        val item = dto.toDomain()

        assertEquals("TV Name", item.title)
    }

    @Test
    fun `KnownForDto toDomain uses Unknown when both title and name are null`() {
        val dto = KnownForDto(
            id = 1,
            title = null,
            name = null,
            mediaType = "movie",
            posterPath = "/poster.jpg",
            overview = "Overview",
            voteAverage = 8.0
        )

        val item = dto.toDomain()

        assertEquals("Unknown", item.title)
    }

    private fun createPersonDto(
        id: Int = 1,
        name: String = "John Doe",
        profilePath: String? = "/profile.jpg",
        knownForDepartment: String? = "Acting",
        knownFor: List<KnownForDto>? = listOf(
            KnownForDto(1, "Movie 1", null, "movie", "/poster1.jpg", "Overview", 8.0),
            KnownForDto(2, "Movie 2", null, "movie", "/poster2.jpg", "Overview", 7.5)
        )
    ) = PersonDto(
        id = id,
        name = name,
        profilePath = profilePath,
        popularity = 100.0,
        knownForDepartment = knownForDepartment,
        gender = 2,
        adult = false,
        knownFor = knownFor
    )

    private fun createPersonDetailResponse(
        biography: String? = "Biography text",
        alsoKnownAs: List<String>? = listOf("Johnny", "J.D.")
    ) = PersonDetailResponse(
        id = 1,
        name = "John Doe",
        biography = biography,
        birthday = "1980-01-15",
        deathday = null,
        placeOfBirth = "Los Angeles, USA",
        profilePath = "/profile.jpg",
        popularity = 100.0,
        knownForDepartment = "Acting",
        homepage = "https://johndoe.com",
        imdbId = "nm1234567",
        alsoKnownAs = alsoKnownAs,
        adult = false,
        gender = 2
    )
}
