package com.cinema.core.ai.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ResolvedIntentDTO(
    val intentId: String,
    val parameters: Map<String, String> = emptyMap(),
    val navigationTarget: String? = null,
    val confidence: Float = 0f,
    val fallbackMessage: String? = null
)
