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

    private val _favoriteMovies = MutableStateFlow<List<FavoriteMovieEntity>>(emptyList())
    private val _favoritePeople = MutableStateFlow<List<FavoritePersonEntity>>(emptyList())

    init {
        scope.launch {
            _favoriteMovies.value = secureLocalDataSource.get<List<FavoriteMovieEntity>>(KEY_FAVORITE_MOVIES) ?: emptyList()
            _favoritePeople.value = secureLocalDataSource.get<List<FavoritePersonEntity>>(KEY_FAVORITE_PEOPLE) ?: emptyList()
        }
    }

    override val favoriteMovies: Flow<List<FavoriteMovie>> =
        _favoriteMovies.map { entities -> entities.map { it.toDomain() } }

    override val favoritePeople: Flow<List<FavoritePerson>> =
        _favoritePeople.map { entities -> entities.map { it.toDomain() } }

    override fun isMovieFavorite(movieId: Int): Flow<Boolean> =
        _favoriteMovies.map { movies -> movies.any { it.id == movieId } }

    override fun isPersonFavorite(personId: Int): Flow<Boolean> =
        _favoritePeople.map { people -> people.any { it.id == personId } }

    override suspend fun addFavoriteMovie(movie: FavoriteMovie) {
        _favoriteMovies.update { currentList -> currentList + movie.toEntity() }
        secureLocalDataSource.save(KEY_FAVORITE_MOVIES, _favoriteMovies.value)
    }

    override suspend fun removeFavoriteMovie(movieId: Int) {
        _favoriteMovies.update { currentList -> currentList.filter { it.id != movieId } }
        secureLocalDataSource.save(KEY_FAVORITE_MOVIES, _favoriteMovies.value)
    }

    override suspend fun addFavoritePerson(person: FavoritePerson) {
        _favoritePeople.update { currentList -> currentList + person.toEntity() }
        secureLocalDataSource.save(KEY_FAVORITE_PEOPLE, _favoritePeople.value)
    }

    override suspend fun removeFavoritePerson(personId: Int) {
        _favoritePeople.update { currentList -> currentList.filter { it.id != personId } }
        secureLocalDataSource.save(KEY_FAVORITE_PEOPLE, _favoritePeople.value)
    }

    companion object {
        private const val KEY_FAVORITE_MOVIES = "favorite_movies"
        private const val KEY_FAVORITE_PEOPLE = "favorite_people"
    }
}
