package com.casemapai.data.remote.dto

import com.squareup.moshi.JsonClass

/*
 * Firebase *callable* functions (onCall) use a fixed HTTP protocol:
 *   request body  = { "data":   <payload> }
 *   response body = { "result": <payload> }
 * Retrofit doesn't know that, so we wrap/unwrap explicitly.
 */
@JsonClass(generateAdapter = true)
data class CallableRequest<T>(val data: T)

@JsonClass(generateAdapter = true)
data class CallableResponse<T>(val result: T)

// ---------- Request ----------

@JsonClass(generateAdapter = true)
data class InterviewTurnRequestDto(
    val seed: InterviewSeedDto,
    val history: List<InterviewTurnDto>,
    val facts: List<CaseFactDto>,
    val forceRecommendation: Boolean
)

@JsonClass(generateAdapter = true)
data class InterviewSeedDto(
    val caseType: String,
    val posture: String,
    val summary: String,
    val goal: String
)

@JsonClass(generateAdapter = true)
data class InterviewTurnDto(
    val question: String,
    val answer: String,
    val wasCustom: Boolean
)

@JsonClass(generateAdapter = true)
data class CaseFactDto(
    val label: String,
    val value: String
)

// ---------- Response ----------

@JsonClass(generateAdapter = true)
data class InterviewTurnResponseDto(
    val kind: String,
    val question: InterviewQuestionDto? = null,
    val recommendation: RecommendationDto? = null,
    val facts: List<CaseFactDto> = emptyList(),
    val turnNumber: Int = 1,
    val maxTurns: Int = 8,
    val modelName: String = ""
)

@JsonClass(generateAdapter = true)
data class InterviewQuestionDto(
    val text: String,
    val why: String = "",
    val options: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class RecommendationDto(
    val summary: String,
    val approaches: List<ApproachDto> = emptyList(),
    val missingFacts: List<String> = emptyList(),
    val verifyBeforeRelying: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ApproachDto(
    val title: String,
    val rationale: String = "",
    val risks: String = ""
)
