package com.casemapai.data.mapper

import com.casemapai.data.remote.dto.CaseFactDto
import com.casemapai.data.remote.dto.InterviewSeedDto
import com.casemapai.data.remote.dto.InterviewTurnDto
import com.casemapai.data.remote.dto.InterviewTurnResponseDto
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewQuestion
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.model.interview.StrategyApproach
import com.casemapai.domain.model.interview.StrategyRecommendation

fun InterviewSeed.toDto() = InterviewSeedDto(caseType, posture, summary, goal)
fun InterviewTurn.toDto() = InterviewTurnDto(question, answer, wasCustom)
fun CaseFact.toDto() = CaseFactDto(label, value)
fun CaseFactDto.toDomain() = CaseFact(label, value)

/** The server already validates, but the client never trusts the wire: re-check the invariants the UI depends on. */
fun InterviewTurnResponseDto.toDomain(): InterviewStep {
    val domainFacts = facts.map { it.toDomain() }
    val options = question?.options.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }.distinct().take(4)

    return when {
        kind == "QUESTION" && question != null && question.text.isNotBlank() && options.size >= 2 ->
            InterviewStep.Question(
                question = InterviewQuestion(question.text, question.why, options),
                facts = domainFacts,
                turnNumber = turnNumber,
                maxTurns = maxTurns
            )

        recommendation != null && recommendation.summary.isNotBlank() ->
            InterviewStep.Recommendation(
                recommendation = StrategyRecommendation(
                    summary = recommendation.summary,
                    approaches = recommendation.approaches.take(3).map {
                        StrategyApproach(it.title, it.rationale, it.risks)
                    },
                    missingFacts = recommendation.missingFacts,
                    verifyBeforeRelying = recommendation.verifyBeforeRelying
                ),
                facts = domainFacts,
                turnNumber = turnNumber,
                maxTurns = maxTurns
            )

        else -> throw IllegalStateException("The AI returned an incomplete step. Please retry.")
    }
}
