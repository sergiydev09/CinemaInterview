package com.cinema.core.favorites.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.data.datasource.get
import com.cinema.core.data.datasource.save
import com.cinema.core.favorites.data.mapper.toDomain
import com.cinema.core.favorites.data.mapper.toEntity
import com.cinema.core.favorites.data.model.FavoriteMovieEntity
import com.cinema.core.favorites.data.model.FavoritePersonEntity
import com.cinema.core.favorites.domain.model.FavoriteMovie
import com.cinema.core.favorites.domain.model.FavoritePerson
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val secureLocalDataSource: SecureLocalDataSource
) : FavoritesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _favoriteMovies = MutableStateFlow<Map<Int, FavoriteMovieEntity>>(emptyMap())
    private val _favoritePeople = MutableStateFlow<Map<Int, FavoritePersonEntity>>(emptyMap())

    init {
        scope.launch {
            _favoriteMovies.value = secureLocalDataSource.get<Map<Int, FavoriteMovieEntity>>(KEY_FAVORITE_MOVIES) ?: emptyMap()
            _favoritePeople.value = secureLocalDataSource.get<Map<Int, FavoritePersonEntity>>(KEY_FAVORITE_PEOPLE) ?: emptyMap()
        }
    }

    override val favoriteMovies: Flow<Map<Int, FavoriteMovie>> =
        _favoriteMovies.map { map -> map.mapValues { it.value.toDomain() } }

    override val favoritePeople: Flow<Map<Int, FavoritePerson>> =
        _favoritePeople.map { map -> map.mapValues { it.value.toDomain() } }

    override fun isMovieFavorite(movieId: Int): Flow<Boolean> =
        _favoriteMovies.map { it.containsKey(movieId) }

    override fun isPersonFavorite(personId: Int): Flow<Boolean> =
        _favoritePeople.map { it.containsKey(personId) }

    override suspend fun addFavoriteMovie(movie: FavoriteMovie) {
        _favoriteMovies.update { map -> map + (movie.id to movie.toEntity()) }
        persistMovies()
    }

    override suspend fun removeFavoriteMovie(movieId: Int) {
        _favoriteMovies.update { map -> map - movieId }
        persistMovies()
    }

    override suspend fun addFavoritePerson(person: FavoritePerson) {
        _favoritePeople.update { map -> map + (person.id to person.toEntity()) }
        persistPeople()
    }

    override suspend fun removeFavoritePerson(personId: Int) {
        _favoritePeople.update { map -> map - personId }
        persistPeople()
    }

    private suspend fun persistMovies() {
        secureLocalDataSource.save(KEY_FAVORITE_MOVIES, _favoriteMovies.value)
    }

    private suspend fun persistPeople() {
        secureLocalDataSource.save(KEY_FAVORITE_PEOPLE, _favoritePeople.value)
    }

    companion object {
        private const val KEY_FAVORITE_MOVIES = "favorite_movies"
        private const val KEY_FAVORITE_PEOPLE = "favorite_people"
    }
}
