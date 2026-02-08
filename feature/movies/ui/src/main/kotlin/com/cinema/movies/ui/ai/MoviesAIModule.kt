package com.cinema.movies.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesAIModule {

    @Binds
    @IntoSet
    abstract fun bindMoviesAIIntentHandler(handler: MoviesAIIntentHandler): AIIntentHandler
}
