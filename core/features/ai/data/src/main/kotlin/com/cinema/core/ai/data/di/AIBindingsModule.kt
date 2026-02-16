package com.cinema.core.ai.data.di

import com.cinema.core.ai.data.manager.AIManagerImpl
import com.cinema.core.ai.data.repository.AIRepositoryImpl
import com.cinema.core.ai.domain.manager.AIManager
import com.cinema.core.ai.domain.repository.AIRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AIBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAIManager(impl: AIManagerImpl): AIManager

    @Binds
    @Singleton
    abstract fun bindAIRepository(impl: AIRepositoryImpl): AIRepository
}
