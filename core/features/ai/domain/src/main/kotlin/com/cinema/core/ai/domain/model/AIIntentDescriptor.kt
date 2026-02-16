package com.cinema.core.ai.domain.model

data class AIIntentDescriptor(
    val intentId: String,
    val description: String,
    val parameters: List<AIIntentParameter>,
    val requiredScreen: String?
)

data class AIIntentParameter(
    val name: String,
    val description: String,
    val type: String,
    val required: Boolean
)
