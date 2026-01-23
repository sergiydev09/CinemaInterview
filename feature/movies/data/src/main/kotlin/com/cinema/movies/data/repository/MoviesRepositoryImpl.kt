package com.cinema.movies.data.repository

import com.cinema.movies.data.datasource.MoviesRemoteDataSource
import com.cinema.movies.data.mapper.MovieMapper.toDomain
import com.cinema.movies.data.mapper.MovieMapper.toDomainList
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.repository.MoviesRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val remoteDataSource: MoviesRemoteDataSource
) : MoviesRepository {

    override suspend fun getTrendingMovies(timeWindow: String): List<Movie> {
        return remoteDataSource.getTrendingMovies(timeWindow).toDomainList()
    }

    override suspend fun getMovieDetail(movieId: Int): MovieDetail {
        return remoteDataSource.getMovieDetail(movieId).toDomain()
    }
}
