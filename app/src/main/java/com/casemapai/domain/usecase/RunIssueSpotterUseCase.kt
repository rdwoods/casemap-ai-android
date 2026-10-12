package com.casemapai.domain.usecase

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.AnalysisType
import com.casemapai.domain.model.Matter
import com.casemapai.domain.repository.AnalysisRepository

class RunIssueSpotterUseCase(
    private val analysisRepository: AnalysisRepository
) {
    suspend operator fun invoke(matter: GetMatterQuery.Data.Matter) = analysisRepository.analyzeText(matter = matter, analysisType = AnalysisType.ISSUE_SPOTTER)
}
