package com.casemapai.domain.model

import java.util.UUID

data class AnalysisResult(
    val id: Long,
    val matterId: UUID,
    val analysisType: AnalysisType,
    val body: String,
    val provider: AiProvider,
    val modelName: String,
    val createdAt: Long
)