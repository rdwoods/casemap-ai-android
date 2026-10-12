/*
package com.casemapai.data.remote.dto

import com.squareup.moshi.JsonClass

data class GeminiGenerateContentRequestDto(
    val contents: List<GeminiContentDto>,
    val generationConfig: GeminiGenerationConfigDto? = null
) {
    data class GeminiContentDto(
        val geminiPartDtos: List<GeminiPartDto>
    )

    @JsonClass(generateAdapter = true)
    data class GeminiGenerationConfigDto(
        val temperature: Float? = null,
        val maxOutputTokens: Int? = null
    )

    data class GeminiPartDto(
        val text: String
    )

    companion object {
        fun fromInput(input: String): GeminiGenerateContentRequestDto {
            return GeminiGenerateContentRequestDto(
                contents = listOf(
                    GeminiContentDto(
                        geminiPartDtos = listOf(
                            GeminiPartDto(text = input)
                        )
                    )
                )
            )
        }
    }
}*/
