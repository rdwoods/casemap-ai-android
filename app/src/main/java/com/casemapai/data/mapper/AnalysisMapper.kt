package com.casemapai.data.mapper

import com.casemapai.data.remote.dto.GeminiGenerateContentResponseDto
import com.casemapai.data.response.OpenAiResponseDto
import com.casemapai.domain.model.AiProvider
import com.casemapai.domain.model.AnalysisResult
import com.casemapai.domain.model.AnalysisType
import java.util.UUID
import javax.inject.Inject
import kotlin.Long

class AnalysisMapper @Inject constructor() {

    fun fromOpenAiDto(
        dto: OpenAiResponseDto,
        matterId: UUID,
        resultId: Long,
        createdAt: Long
    ): AnalysisResult {
        return AnalysisResult(
            id = resultId,
            matterId = matterId,
            analysisType = AnalysisType.ISSUE_SPOTTER,
            body = dto.extractText(),
            provider = AiProvider.OPENAI,
            modelName = "gpt-4o-mini",
            createdAt = createdAt
        )
    }

    fun fromGeminiDto(
        dto: GeminiGenerateContentResponseDto,
        matterId: UUID,
        resultId: Long,
        analysisType: AnalysisType = AnalysisType.ISSUE_SPOTTER,
        createdAt: Long,
        modelName: String = "gemini-2.5-flash"
    ): AnalysisResult {
        return AnalysisResult(
            id = resultId,
            matterId = matterId,
            analysisType = analysisType,
            body = dto.extractText(),
            provider = AiProvider.GEMINI,
            createdAt = createdAt,
            modelName = modelName
        )
    }

    /*fun toDomain(dto: GeminiResponseDto): AnalysisResult {
        val text = dto.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text
            ?: "No response"

        return AnalysisResult(
            resultText = text
        )
    }*/
}