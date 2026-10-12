package com.casemapai.domain.usecase

import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.repository.InterviewRepository

class NextInterviewStepUseCase(
    private val repository: InterviewRepository
) {
    suspend operator fun invoke(
        seed: InterviewSeed,
        history: List<InterviewTurn>,
        facts: List<CaseFact>,
        forceRecommendation: Boolean = false
    ): InterviewStep {
        require(seed.isValid) { "Case type and summary are required." }
        return repository.nextStep(seed, history, facts, forceRecommendation)
    }
}
