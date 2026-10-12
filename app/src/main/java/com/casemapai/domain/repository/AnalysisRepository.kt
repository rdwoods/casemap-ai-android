package com.casemapai.domain.repository

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.AnalysisResult
import com.casemapai.domain.model.AnalysisType
import com.casemapai.domain.model.GeminiAnalysisResult
import com.casemapai.domain.model.Matter
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface AnalysisRepository {

    suspend fun analyzeText(
        matter: GetMatterQuery.Data.Matter,
        analysisType: AnalysisType
    ): AnalysisResult

    //suspend fun runIssueSpotter(matter: Matter): AnalysisResult

    fun observeResults(matterId: UUID): Flow<List<AnalysisResult>>
}