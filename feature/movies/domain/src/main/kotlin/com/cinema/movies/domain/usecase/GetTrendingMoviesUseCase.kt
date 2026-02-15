package com.cinema.movies.domain.usecase

import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingMoviesUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository
) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Movie>>> =
        moviesRepository.getTrendingMovies(timeWindow.value).asResult()
}
