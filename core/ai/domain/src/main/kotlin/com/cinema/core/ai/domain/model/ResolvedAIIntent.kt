package com.cinema.core.ai.domain.model

data class ResolvedAIIntent(
    val intentId: String,
    val parameters: Map<String, String>,
    val navigationTarget: String?,
    val confidence: Float,
    val fallbackMessage: String?
)
