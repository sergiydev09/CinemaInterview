package com.cinema.login.data.di

import com.cinema.login.data.repository.LoginRepositoryImpl
import com.cinema.login.data.repository.UserPreferencesRepositoryImpl
import com.cinema.login.domain.repository.LoginRepository
import com.cinema.login.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding Login implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class LoginDataModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(impl: LoginRepositoryImpl): LoginRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
