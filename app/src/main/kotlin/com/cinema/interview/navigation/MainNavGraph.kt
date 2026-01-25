package com.cinema.interview.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cinema.home.ui.feature.HomeScreen
import com.cinema.home.ui.navigation.HomeDeeplinks
import com.cinema.home.ui.navigation.HomeRoute
import com.cinema.movies.ui.feature.detail.MovieDetailScreen
import com.cinema.movies.ui.feature.list.MoviesScreen
import com.cinema.movies.ui.navigation.MovieDetailRoute
import com.cinema.movies.ui.navigation.MoviesDeeplinks
import com.cinema.movies.ui.navigation.MoviesRoute
import com.cinema.people.ui.feature.detail.PersonDetailScreen
import com.cinema.people.ui.feature.list.PeopleScreen
import com.cinema.people.ui.navigation.PeopleDeeplinks
import com.cinema.people.ui.navigation.PeopleRoute
import com.cinema.people.ui.navigation.PersonDetailRoute

@Composable
fun MainNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute>(deepLinks = HomeDeeplinks.home) {
            HomeScreen(
                onMovieClick = { movieId ->
                    navController.navigate(MovieDetailRoute(movieId))
                },
                onPersonClick = { personId ->
                    navController.navigate(PersonDetailRoute(personId))
                }
            )
        }

        composable<MoviesRoute>(deepLinks = MoviesDeeplinks.movies) {
            MoviesScreen(
                onMovieClick = { movieId ->
                    navController.navigate(MovieDetailRoute(movieId))
                }
            )
        }

        composable<MovieDetailRoute>(deepLinks = MoviesDeeplinks.movieDetail) {
            MovieDetailScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<PeopleRoute>(deepLinks = PeopleDeeplinks.people) {
            PeopleScreen(
                onPersonClick = { personId ->
                    navController.navigate(PersonDetailRoute(personId))
                }
            )
        }

        composable<PersonDetailRoute>(deepLinks = PeopleDeeplinks.personDetail) {
            PersonDetailScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}
