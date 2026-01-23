package com.cinema.people.domain.model

data class Person(
    val id: Int,
    val name: String,
    val profileUrl: String?,
    val popularity: Double,
    val knownForDepartment: String,
    val knownFor: List<KnownForItem>
)

data class PersonDetail(
    val id: Int,
    val name: String,
    val biography: String,
    val birthday: String?,
    val deathday: String?,
    val placeOfBirth: String?,
    val profileUrl: String?,
    val popularity: Double,
    val knownForDepartment: String,
    val homepage: String?,
    val alsoKnownAs: List<String>
)

data class KnownForItem(
    val id: Int,
    val title: String,
    val mediaType: String,
    val posterUrl: String?
)
