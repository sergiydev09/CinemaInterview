package com.cinema.home.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.data.datasource.observe
import com.cinema.home.data.mapper.toDomain
import com.cinema.home.data.model.FavoriteMovieDTO
import com.cinema.home.data.model.FavoritePersonDTO
import com.cinema.home.domain.model.FavoriteMovie
import com.cinema.home.domain.model.FavoritePerson
import com.cinema.home.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    secureLocalDataSource: SecureLocalDataSource
) : FavoritesRepository {

    override val favoriteMovies: Flow<List<FavoriteMovie>> =
        secureLocalDataSource.observe<Map<Int, FavoriteMovieDTO>>(KEY_FAVORITE_MOVIES)
            .map { map -> map?.values?.map { it.toDomain() } ?: emptyList() }

    override val favoritePeople: Flow<List<FavoritePerson>> =
        secureLocalDataSource.observe<Map<Int, FavoritePersonDTO>>(KEY_FAVORITE_PEOPLE)
            .map { map -> map?.values?.map { it.toDomain() } ?: emptyList() }

    companion object {
        private const val KEY_FAVORITE_MOVIES = "favorite_movies"
        private const val KEY_FAVORITE_PEOPLE = "favorite_people"
    }
}
