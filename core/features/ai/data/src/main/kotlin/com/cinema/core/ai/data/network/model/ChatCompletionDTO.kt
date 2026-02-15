package com.cinema.core.ai.data.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequestDTO(
    val model: String,
    val messages: List<ChatMessageDTO>,
    @SerialName("response_format") val responseFormat: ResponseFormatDTO? = null
)

@Serializable
data class ChatMessageDTO(
    val role: String,
    val content: String
)

@Serializable
data class ResponseFormatDTO(
    val type: String
)

@Serializable
data class ChatCompletionResponseDTO(
    val choices: List<ChoiceDTO>
)

@Serializable
data class ChoiceDTO(
    val message: ChatMessageDTO
)
