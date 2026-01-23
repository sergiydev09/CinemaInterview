package com.cinema.people.data.di

import com.cinema.people.data.network.PeopleApiService
import com.cinema.people.data.repository.PeopleRepositoryImpl
import com.cinema.people.domain.repository.PeopleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Hilt module providing People network dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object PeopleNetworkModule {

    @Provides
    @Singleton
    fun providePeopleApiService(retrofit: Retrofit): PeopleApiService =
        retrofit.create(PeopleApiService::class.java)
}

/**
 * Hilt module for binding People implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PeopleBindingsModule {

    @Binds
    @Singleton
    abstract fun bindPeopleRepository(impl: PeopleRepositoryImpl): PeopleRepository
}
