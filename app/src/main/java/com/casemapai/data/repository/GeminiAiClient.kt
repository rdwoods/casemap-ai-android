package com.casemapai.data.repository

import com.casemapai.data.remote.dto.AnalyzeRequestDto
import com.casemapai.data.service.GeminiApiService
import com.casemapai.domain.model.AiProvider
import javax.inject.Inject

class GeminiAiClient @Inject constructor(
    private val api: GeminiApiService
) : AiClient {

    override suspend fun analyze(prompt: String): AiResponse {
        val dto = api.generateContent(
            request = AnalyzeRequestDto(prompt = prompt)
        )

        val provider = when (dto.provider.uppercase()) {
            "GEMINI" -> AiProvider.GEMINI
            else -> AiProvider.GEMINI
        }

        return AiResponse(
            body = dto.body,
            provider = provider,
            modelName = dto.modelName
        )
    }
}
