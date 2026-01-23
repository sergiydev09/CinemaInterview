package com.cinema.movies.data.network

import com.cinema.movies.data.network.model.MovieDetailResponse
import com.cinema.movies.data.network.model.TrendingMoviesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoviesApiService {

    @GET("trending/movie/{time_window}")
    suspend fun getTrendingMovies(
        @Path("time_window") timeWindow: String = "day",
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): TrendingMoviesResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String = "en-US"
    ): MovieDetailResponse
}
