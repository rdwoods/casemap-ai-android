package com.casemapai.domain.repository

import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn

interface InterviewRepository {

    /**
     * Sends the whole interview state and gets the next step back.
     * Stateless on purpose: the client owns the history, so editing an earlier
     * answer is just "truncate the list and call again".
     */
    suspend fun nextStep(
        seed: InterviewSeed,
        history: List<InterviewTurn>,
        facts: List<CaseFact>,
        forceRecommendation: Boolean = false
    ): InterviewStep
}
