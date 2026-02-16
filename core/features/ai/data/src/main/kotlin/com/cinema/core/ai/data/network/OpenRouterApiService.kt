package com.cinema.core.ai.data.network

import com.cinema.core.ai.data.network.model.ChatCompletionRequestDTO
import com.cinema.core.ai.data.network.model.ChatCompletionResponseDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApiService {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequestDTO
    ): ChatCompletionResponseDTO
}
