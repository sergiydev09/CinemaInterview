package com.cinema.core.ai.domain.repository

import com.cinema.core.ai.domain.model.ResolvedAIIntent

interface AIRepository {
    suspend fun resolveIntent(
        text: String,
        availableIntents: String,
        currentScreen: String?,
        screenContext: String? = null,
        isFollowUp: Boolean = false
    ): ResolvedAIIntent
}
