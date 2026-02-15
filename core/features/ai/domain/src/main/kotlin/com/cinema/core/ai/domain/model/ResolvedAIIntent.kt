package com.cinema.core.ai.domain.model

data class ResolvedAIIntent(
    val intentId: String,
    val parameters: Map<String, String> = emptyMap(),
    val navigationTarget: String? = null,
    val confidence: Float = 0f,
    val fallbackMessage: String? = null,
    val needsFollowUp: Boolean = false,
    val nextIntents: List<ResolvedAIIntent> = emptyList()
)
