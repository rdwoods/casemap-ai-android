package com.casemapai.data.repository

import com.casemapai.data.mapper.toDomain
import com.casemapai.data.mapper.toDto
import com.casemapai.data.remote.dto.CallableRequest
import com.casemapai.data.remote.dto.InterviewTurnRequestDto
import com.casemapai.data.service.InterviewApiService
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.repository.InterviewRepository
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(
    private val api: InterviewApiService
) : InterviewRepository {

    override suspend fun nextStep(
        seed: InterviewSeed,
        history: List<InterviewTurn>,
        facts: List<CaseFact>,
        forceRecommendation: Boolean
    ): InterviewStep {
        val request = InterviewTurnRequestDto(
            seed = seed.toDto(),
            history = history.map { it.toDto() },
            facts = facts.map { it.toDto() },
            forceRecommendation = forceRecommendation
        )
        return api.nextTurn(CallableRequest(request)).result.toDomain()
    }
}
