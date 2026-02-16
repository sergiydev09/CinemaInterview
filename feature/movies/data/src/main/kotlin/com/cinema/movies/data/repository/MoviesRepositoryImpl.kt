package com.cinema.movies.data.repository

import com.cinema.core.data.datasource.SecureLocalDataSource
import com.cinema.core.data.datasource.get
import com.cinema.core.data.datasource.observe
import com.cinema.core.data.datasource.save
import com.cinema.movies.data.datasource.MoviesRemoteDataSource
import com.cinema.movies.data.mapper.MovieMapper.toDomain
import com.cinema.movies.data.mapper.MovieMapper.toDomainList
import com.cinema.movies.data.model.FavoriteMovieDTO
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val remoteDataSource: MoviesRemoteDataSource,
    private val secureLocalDataSource: SecureLocalDataSource
) : MoviesRepository {

    override fun getTrendingMovies(timeWindow: String): Flow<List<Movie>> =
        flow { emit(remoteDataSource.getTrendingMovies(timeWindow).toDomainList()) }
            .combine(
                secureLocalDataSource.observe<Map<Int, FavoriteMovieDTO>>(KEY_FAVORITE_MOVIES)
            ) { movies, favorites ->
                val favoriteIds = favorites?.keys ?: emptySet()
                movies.map { it.copy(isFavorite = it.id in favoriteIds) }
            }

    override suspend fun getMovieDetail(movieId: Int): MovieDetail =
        remoteDataSource.getMovieDetail(movieId).toDomain()

    override fun isMovieFavorite(movieId: Int): Flow<Boolean> =
        secureLocalDataSource.observe<Map<Int, FavoriteMovieDTO>>(KEY_FAVORITE_MOVIES)
            .map { it?.containsKey(movieId) ?: false }

    override suspend fun toggleFavoriteMovie(id: Int, title: String, posterUrl: String?, releaseDate: String?) {
        val current = secureLocalDataSource.get<Map<Int, FavoriteMovieDTO>>(KEY_FAVORITE_MOVIES) ?: emptyMap()
        val updated = if (current.containsKey(id)) current - id
        else current + (id to FavoriteMovieDTO(id, title, posterUrl, releaseDate))
        secureLocalDataSource.save(KEY_FAVORITE_MOVIES, updated)
    }

    companion object {
        private const val KEY_FAVORITE_MOVIES = "favorite_movies"
    }
}
