package com.cinema.core.ai.domain.handler

import com.cinema.core.ai.domain.model.AIIntent
import com.cinema.core.ai.domain.model.AIIntentDescriptor
import com.cinema.core.ai.domain.model.ResolvedAIIntent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

abstract class AIIntentHandler {

    abstract val featureId: String
    abstract fun getDescriptors(): List<AIIntentDescriptor>
    abstract fun resolve(resolvedIntent: ResolvedAIIntent): AIIntent?

    open fun getScreenContext(): String? = null

    @Volatile
    private var screenDataDeferred: CompletableDeferred<Unit>? = null

    fun prepareForScreenDataUpdate() {
        screenDataDeferred = CompletableDeferred()
    }

    fun notifyScreenDataLoadCompleted() {
        screenDataDeferred?.complete(Unit)
    }

    suspend fun awaitScreenDataUpdate() {
        screenDataDeferred?.await()
    }

    private val _intents = MutableSharedFlow<AIIntent>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    @Volatile
    private var pendingIntent: AIIntent? = null

    val intents: Flow<AIIntent> = flow {
        val pending = pendingIntent
        if (pending != null) {
            pendingIntent = null
            emit(pending)
        }
        _intents.collect {
            pendingIntent = null
            emit(it)
        }
    }

    fun dispatch(intent: AIIntent) {
        pendingIntent = intent
        _intents.tryEmit(intent)
    }
}
