package com.cinema.movies.domain.repository

import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun getTrendingMovies(timeWindow: String): Flow<List<Movie>>
    suspend fun getMovieDetail(movieId: Int): MovieDetail
    fun isMovieFavorite(movieId: Int): Flow<Boolean>
    suspend fun toggleFavoriteMovie(id: Int, title: String, posterUrl: String?, releaseDate: String?)
}
