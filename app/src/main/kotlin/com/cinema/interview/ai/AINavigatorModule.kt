package com.cinema.interview.ai

import com.cinema.core.ai.domain.manager.AINavigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AINavigatorModule {

    @Binds
    @Singleton
    abstract fun bindAINavigator(impl: AINavigatorImpl): AINavigator
}
