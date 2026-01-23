package com.cinema.movies.domain.repository

import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail

interface MoviesRepository {

    suspend fun getTrendingMovies(timeWindow: String): List<Movie>

    suspend fun getMovieDetail(movieId: Int): MovieDetail
}
