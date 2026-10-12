package com.casemapai.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnalyzeRequestDto(
    val prompt: String
)

@JsonClass(generateAdapter = true)
data class AnalyzeResponseDto(
    val body: String,
    val provider: String,
    val modelName: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentRequestDto(
    val contents: List<GeminiContentDto>,
    val generationConfig: GeminiGenerationConfigDto? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContentDto(
    val parts: List<GeminiPartDto>
)

@JsonClass(generateAdapter = true)
data class GeminiPartDto(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfigDto(
    val temperature: Float? = null,
    val maxOutputTokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerateContentResponseDto(
    val candidates: List<GeminiCandidateDto>? = null
) {
    fun extractText(): String {
        return candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("\n") { it.text.orEmpty() }
            ?.ifBlank { null }
            ?: "No analysis text returned."
    }
}

@JsonClass(generateAdapter = true)
data class GeminiCandidateDto(
    val content: GeminiContentResponseDto? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContentResponseDto(
    val parts: List<GeminiTextPartDto>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTextPartDto(
    val text: String? = null
)
