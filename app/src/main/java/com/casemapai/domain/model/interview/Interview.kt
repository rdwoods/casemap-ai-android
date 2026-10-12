package com.casemapai.domain.model.interview

/** The few fields the attorney types up front. Everything else is gathered by the interview. */
data class InterviewSeed(
    val caseType: String,
    val posture: String,
    val summary: String,
    val goal: String = ""
) {
    val isValid: Boolean get() = caseType.isNotBlank() && summary.isNotBlank()
}

/** One answered question. [wasCustom] = the attorney typed their own answer instead of tapping an option. */
data class InterviewTurn(
    val question: String,
    val answer: String,
    val wasCustom: Boolean
)

/** A structured fact the model extracted from the answers. The interview's by-product is a case record. */
data class CaseFact(
    val label: String,
    val value: String
)

data class InterviewQuestion(
    val text: String,
    val why: String,
    val options: List<String>
)

data class StrategyApproach(
    val title: String,
    val rationale: String,
    val risks: String
)

data class StrategyRecommendation(
    val summary: String,
    val approaches: List<StrategyApproach>,
    val missingFacts: List<String>,
    val verifyBeforeRelying: List<String>
)

/** What the AI returns for each turn: either the next question or the final recommendation. */
sealed interface InterviewStep {
    val facts: List<CaseFact>
    val turnNumber: Int
    val maxTurns: Int

    data class Question(
        val question: InterviewQuestion,
        override val facts: List<CaseFact>,
        override val turnNumber: Int,
        override val maxTurns: Int
    ) : InterviewStep

    data class Recommendation(
        val recommendation: StrategyRecommendation,
        override val facts: List<CaseFact>,
        override val turnNumber: Int,
        override val maxTurns: Int
    ) : InterviewStep
}
