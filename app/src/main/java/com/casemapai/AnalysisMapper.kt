package com.casemapai

import com.casemapai.data.entity.AnalysisResultEntity
import com.casemapai.domain.model.AnalysisResult
import com.casemapai.domain.model.AnalysisType

object AnalysisMapper {

    fun toEntity(domain: AnalysisResult): AnalysisResultEntity {
        return AnalysisResultEntity(
            id = domain.id,
            matterId = domain.matterId,
            analysisType = domain.analysisType.name,
            promptUsed = "", // fill later when you wire prompt builder
            rawResponse = domain.body,
            createdAt = System.currentTimeMillis()
        )
    }

    /*fun fromEntity(entity: AnalysisResultEntity): AnalysisResult {
        return AnalysisResult(
            id = entity.id,
            matterId = entity.matterId,
            analysisType = AnalysisType.valueOf(entity.analysisType),
            body = entity.rawResponse,
            createdAtLabel = formatTimestamp(entity.createdAt)
        )
    }*/

    private fun formatTimestamp(timestamp: Long): String {
        return "Recently"
    }
}