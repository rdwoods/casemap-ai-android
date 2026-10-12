package com.casemapai.domain.model

data class GeminiAnalysisResult(
    val id: Long,
    val matterId: Long,
    val analysisType: AnalysisType,
    val body: String,
    val createdAt: Long,
    val createdAtLabel: String
)