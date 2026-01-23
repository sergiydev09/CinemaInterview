package com.cinema.movies.data.di

import com.cinema.movies.data.network.MoviesApiService
import com.cinema.movies.data.repository.MoviesRepositoryImpl
import com.cinema.movies.domain.repository.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module providing Movies network dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object MoviesNetworkModule {

    @Provides
    @Singleton
    fun provideMoviesApiService(retrofit: Retrofit): MoviesApiService =
        retrofit.create(MoviesApiService::class.java)
}

/**
 * Hilt module for binding Movies implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesBindingsModule {

    @Binds
    @Singleton
    abstract fun bindMoviesRepository(impl: MoviesRepositoryImpl): MoviesRepository
}
