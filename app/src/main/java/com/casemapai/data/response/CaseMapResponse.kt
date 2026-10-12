package com.casemapai.data.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiRequestDto(
    @Json(name = "model")
    val model: String,
    @Json(name = "input")
    val input: String
)

@JsonClass(generateAdapter = true)
data class OpenAiResponseDto(
    @Json(name = "id")
    val id: String? = null,
    @Json(name = "output")
    val output: List<OutputItemDto>? = null,
    @Json(name = "output_text")
    val outputText: String? = null
) {
    fun extractText(): String {
        if (!outputText.isNullOrBlank()) return outputText

        val textFromOutput = output
            ?.flatMap { it.content.orEmpty() }
            ?.mapNotNull { it.text }
            ?.joinToString("\n")
            ?.trim()

        return if (!textFromOutput.isNullOrBlank()) {
            textFromOutput
        } else {
            "No analysis text was returned."
        }
    }
}

@JsonClass(generateAdapter = true)
data class OutputItemDto(
    @Json(name = "content")
    val content: List<ContentItemDto>? = null
)

@JsonClass(generateAdapter = true)
data class ContentItemDto(
    @Json(name = "text")
    val text: String? = null
)