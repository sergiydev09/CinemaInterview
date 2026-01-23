package com.cinema.movies.data.datasource

import com.cinema.movies.data.network.MoviesApiService
import com.cinema.movies.data.network.model.MovieDetailResponse
import com.cinema.movies.data.network.model.MovieDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRemoteDataSource @Inject constructor(
    private val apiService: MoviesApiService
) {
    suspend fun getTrendingMovies(timeWindow: String): List<MovieDto> {
        return apiService.getTrendingMovies(timeWindow).results
    }

    suspend fun getMovieDetail(movieId: Int): MovieDetailResponse {
        return apiService.getMovieDetail(movieId)
    }
}
