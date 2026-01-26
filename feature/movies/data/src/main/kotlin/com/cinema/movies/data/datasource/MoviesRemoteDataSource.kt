package com.cinema.movies.data.datasource

import com.cinema.movies.data.network.MoviesApiService
import com.cinema.movies.data.network.model.MovieDTO
import com.cinema.movies.data.network.model.MovieDetailDTO
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRemoteDataSource @Inject constructor(
    private val apiService: MoviesApiService
) {
    suspend fun getTrendingMovies(timeWindow: String): List<MovieDTO> {
        return apiService.getTrendingMovies(timeWindow).results
    }

    suspend fun getMovieDetail(movieId: Int): MovieDetailDTO {
        return apiService.getMovieDetail(movieId)
    }
}
