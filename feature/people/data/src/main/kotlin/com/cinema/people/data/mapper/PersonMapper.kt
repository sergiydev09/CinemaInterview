package com.cinema.people.data.mapper

import com.cinema.core.data.util.ImageUrlBuilder
import com.cinema.people.data.network.model.KnownForDTO
import com.cinema.people.data.network.model.PersonDTO
import com.cinema.people.data.network.model.PersonDetailDTO
import com.cinema.people.domain.model.KnownForItem
import com.cinema.people.domain.model.Person
import com.cinema.people.domain.model.PersonDetail

object PersonMapper {

    fun PersonDTO.toDomain(): Person {
        return Person(
            id = id,
            name = name,
            profileUrl = ImageUrlBuilder.buildProfileUrl(profilePath),
            popularity = popularity,
            knownForDepartment = knownForDepartment ?: "Unknown",
            knownFor = knownFor?.map { it.toDomain() } ?: emptyList()
        )
    }

    fun PersonDetailDTO.toDomain(): PersonDetail {
        return PersonDetail(
            id = id,
            name = name,
            biography = biography ?: "",
            birthday = birthday,
            deathday = deathday,
            placeOfBirth = placeOfBirth,
            profileUrl = ImageUrlBuilder.buildProfileUrl(profilePath),
            popularity = popularity,
            knownForDepartment = knownForDepartment ?: "Unknown",
            homepage = homepage,
            alsoKnownAs = alsoKnownAs ?: emptyList()
        )
    }

    fun KnownForDTO.toDomain(): KnownForItem {
        return KnownForItem(
            id = id,
            title = title ?: name ?: "Unknown",
            mediaType = mediaType,
            posterUrl = ImageUrlBuilder.buildThumbnailUrl(posterPath)
        )
    }

    fun List<PersonDTO>.toDomainList(): List<Person> {
        return map { it.toDomain() }
    }
}
