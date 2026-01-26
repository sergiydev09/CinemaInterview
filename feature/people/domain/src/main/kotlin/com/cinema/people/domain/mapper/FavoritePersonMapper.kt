package com.cinema.people.domain.mapper

import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.people.domain.model.PersonDetail

fun PersonDetail.toFavoritePerson(): FavoritePerson = FavoritePerson(
    id = id,
    name = name,
    profileUrl = profileUrl
)
