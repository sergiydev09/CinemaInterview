package com.cinema.people.ui.ai

import com.cinema.core.ai.domain.handler.AIIntentHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class PeopleAIModule {

    @Binds
    @IntoSet
    abstract fun bindPeopleAIIntentHandler(handler: PeopleAIIntentHandler): AIIntentHandler
}
