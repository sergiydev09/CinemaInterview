package com.cinema.core.ai.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ChainedIntentDTO(
    val intentId: String,
    val parameters: Map<String, String> = emptyMap(),
    val navigationTarget: String? = null
)

@Serializable
data class ResolvedIntentDTO(
    val intentId: String,
    val parameters: Map<String, String> = emptyMap(),
    val navigationTarget: String? = null,
    val confidence: Float = 0f,
    val fallbackMessage: String? = null,
    val needsFollowUp: Boolean = false,
    val nextIntents: List<ChainedIntentDTO> = emptyList()
)
