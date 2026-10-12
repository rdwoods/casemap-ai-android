package com.casemapai.data.repository

import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewQuestion
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.model.interview.StrategyApproach
import com.casemapai.domain.model.interview.StrategyRecommendation
import com.casemapai.domain.repository.InterviewRepository
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * Offline, scripted stand-in for the AI. Use it for Compose previews, UI work,
 * and unit tests so you're not paying for (or waiting on) Gemini while iterating.
 */
class FakeInterviewRepository @Inject constructor() : InterviewRepository {

    var latencyMs: Long = 600L

    private val script = listOf(
        InterviewQuestion(
            text = "How strong is the documentary corroboration?",
            why = "Corroboration drives credibility findings and how hard to press on cross.",
            options = listOf("Extensive and consistent", "Partial / gaps", "Testimony only", "Unknown / not yet determined")
        ),
        InterviewQuestion(
            text = "Are there inconsistencies between the written statement and prior statements?",
            why = "Material inconsistencies are the main lever on credibility.",
            options = listOf("Material inconsistencies", "Minor only", "None found", "Prior statements not reviewed")
        ),
        InterviewQuestion(
            text = "Is there evidence the harm was tied to a protected ground?",
            why = "Nexus is often the most contestable element.",
            options = listOf("Clear nexus evidence", "Mixed motives", "Appears personal / criminal", "Unknown / not yet determined")
        )
    )

    override suspend fun nextStep(
        seed: InterviewSeed,
        history: List<InterviewTurn>,
        facts: List<CaseFact>,
        forceRecommendation: Boolean
    ): InterviewStep {
        delay(latencyMs)
        val updatedFacts = history.mapIndexed { i, t -> CaseFact("Answer ${i + 1}", t.answer) }
        val turnNumber = history.size + 1

        return if (!forceRecommendation && history.size < script.size) {
            InterviewStep.Question(script[history.size], updatedFacts, turnNumber, maxTurns = 8)
        } else {
            InterviewStep.Recommendation(
                recommendation = StrategyRecommendation(
                    summary = "Scripted recommendation for \"${seed.caseType}\" based on ${history.size} answers.",
                    approaches = listOf(
                        StrategyApproach("Focus on credibility", "Built from the inconsistency answer.", "Judge may find them minor."),
                        StrategyApproach("Contest nexus", "Built from the nexus answer.", "Mixed-motive evidence may suffice."),
                        StrategyApproach("Narrow the issues", "Concede uncontested elements to focus the hearing.", "Reduces fallback positions.")
                    ),
                    missingFacts = listOf("Country-conditions evidence"),
                    verifyBeforeRelying = listOf("Current controlling standard for the one-year filing exception")
                ),
                facts = updatedFacts,
                turnNumber = turnNumber,
                maxTurns = 8
            )
        }
    }
}
