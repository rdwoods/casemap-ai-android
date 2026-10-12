package com.casemapai.ui.interview

import com.casemapai.data.sample.SampleScenario
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewQuestion
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.InterviewTurn
import com.casemapai.domain.model.interview.StrategyRecommendation

enum class InterviewPhase { SEED, INTERVIEWING, DONE }

enum class SeedField { CASE_TYPE, POSTURE, SUMMARY, GOAL }

/**
 * One answered question, kept with the options that were shown and the fact sheet as it stood
 * before the answer. That snapshot is what makes "edit an earlier answer" free: rewind to it,
 * no network call needed.
 */
data class AnsweredQuestion(
    val question: InterviewQuestion,
    val answer: String,
    val wasCustom: Boolean,
    val factsBefore: List<CaseFact>
) {
    fun toTurn() = InterviewTurn(question.text, answer, wasCustom)
}

data class InterviewState(
    val phase: InterviewPhase = InterviewPhase.SEED,
    val seed: InterviewSeed = InterviewSeed(caseType = "", posture = "", summary = ""),
    val transcript: List<AnsweredQuestion> = emptyList(),
    val currentQuestion: InterviewQuestion? = null,
    val facts: List<CaseFact> = emptyList(),
    val recommendation: StrategyRecommendation? = null,
    val customAnswer: String = "",
    val turnNumber: Int = 0,
    val maxTurns: Int = 8,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Remembers whether the failed request was a "finish now", so Retry repeats the same thing. */
    val pendingForceRecommendation: Boolean = false
) {
    val canStart: Boolean get() = seed.isValid && !isLoading
    val canSubmitCustom: Boolean get() = customAnswer.isNotBlank() && !isLoading && currentQuestion != null
    val canFinishEarly: Boolean get() = phase == InterviewPhase.INTERVIEWING && transcript.isNotEmpty() && !isLoading
}

sealed interface InterviewIntent {
    data class SeedChanged(val field: SeedField, val value: String) : InterviewIntent
    data class LoadSample(val scenario: SampleScenario) : InterviewIntent
    data object Start : InterviewIntent

    data class OptionSelected(val option: String) : InterviewIntent
    data class CustomAnswerChanged(val text: String) : InterviewIntent
    data object SubmitCustomAnswer : InterviewIntent

    /** Rewind to question [index]; everything after it is discarded. */
    data class EditAnswer(val index: Int) : InterviewIntent
    data object FinishNow : InterviewIntent
    data object Retry : InterviewIntent
    data object Restart : InterviewIntent
}

sealed interface InterviewEffect {
    data class ShowMessage(val message: String) : InterviewEffect
}
