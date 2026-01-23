package com.cinema.movies.domain.usecase

import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.movies.domain.model.MovieDetail
import com.cinema.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMovieDetailUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository
) {
    operator fun invoke(movieId: Int): Flow<Result<MovieDetail>> = flow {
        emit(moviesRepository.getMovieDetail(movieId))
    }.asResult()
}
