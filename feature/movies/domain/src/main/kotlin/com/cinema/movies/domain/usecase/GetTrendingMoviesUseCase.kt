package com.cinema.movies.domain.usecase

import com.cinema.core.domain.model.TimeWindow
import com.cinema.core.domain.util.Result
import com.cinema.core.domain.util.asResult
import com.cinema.core.favorites.domain.repository.FavoritesRepository
import com.cinema.movies.domain.model.Movie
import com.cinema.movies.domain.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for getting trending movies with favorite status.
 */
class GetTrendingMoviesUseCase @Inject constructor(
    private val moviesRepository: MoviesRepository,
    private val favoritesRepository: FavoritesRepository
) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Movie>>> =
        combine(
            flow { emit(moviesRepository.getTrendingMovies(timeWindow.value)) },
            favoritesRepository.favoriteMovies
        ) { movies, favoritesMap ->
            movies.map { movie -> movie.copy(isFavorite = favoritesMap.containsKey(movie.id)) }
        }.asResult()
}
