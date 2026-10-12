package com.casemapai.ui.interview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.casemapai.domain.model.interview.InterviewStep
import com.casemapai.domain.usecase.GetCaseFileUseCase
import com.casemapai.domain.usecase.NextInterviewStepUseCase
import com.casemapai.domain.usecase.SaveInterviewResultUseCase
import com.casemapai.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class InterviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val nextInterviewStep: NextInterviewStepUseCase,
    private val getCaseFile: GetCaseFileUseCase,
    private val saveInterviewResult: SaveInterviewResultUseCase
) : BaseViewModel<InterviewIntent, InterviewState, InterviewEffect>(InterviewState()) {

    init {
        savedStateHandle.get<String>(ARG_CASE_ID)?.let { raw ->
            viewModelScope.launch { continueExisting(UUID.fromString(raw)) }
        }
    }

    /** "Continue interview" from a saved matter: preload its seed and facts, then ask the next question. */
    private suspend fun continueExisting(caseId: UUID) {
        val file = getCaseFile(caseId)
        if (file == null) {
            updateState { copy(error = "Matter not found.") }
            return
        }
        val facts = file.factsForInterview()
        updateState {
            copy(
                seed = file.seed,
                existingCaseId = file.id,
                existingTitle = file.title,
                initialFacts = facts,
                facts = facts,
                phase = InterviewPhase.INTERVIEWING
            )
        }
        requestNext(force = false)
    }

    override suspend fun reduce(intent: InterviewIntent) {
        when (intent) {
            is InterviewIntent.SeedChanged -> updateSeed(intent.field, intent.value)
            is InterviewIntent.LoadSample -> updateState { copy(seed = intent.scenario.seed, error = null) }
            InterviewIntent.Start -> start()

            is InterviewIntent.OptionSelected -> answer(intent.option, wasCustom = false)
            is InterviewIntent.CustomAnswerChanged -> updateState { copy(customAnswer = intent.text) }
            InterviewIntent.SubmitCustomAnswer -> {
                val text = state.value.customAnswer.trim()
                if (text.isNotEmpty()) answer(text, wasCustom = true)
            }

            is InterviewIntent.EditAnswer -> rewindTo(intent.index)
            InterviewIntent.FinishNow -> if (state.value.canFinishEarly) requestNext(force = true)
            InterviewIntent.Retry -> requestNext(force = state.value.pendingForceRecommendation)
            InterviewIntent.Restart -> updateState {
                InterviewState(
                    seed = seed,
                    existingCaseId = existingCaseId,
                    existingTitle = existingTitle,
                    initialFacts = initialFacts
                )
            }
            InterviewIntent.SaveMatter -> saveMatter()
        }
    }

    private fun updateSeed(field: SeedField, value: String) = updateState {
        copy(
            seed = when (field) {
                SeedField.CASE_TYPE -> seed.copy(caseType = value)
                SeedField.POSTURE -> seed.copy(posture = value)
                SeedField.SUMMARY -> seed.copy(summary = value)
                SeedField.GOAL -> seed.copy(goal = value)
            },
            error = null
        )
    }

    private suspend fun start() {
        if (!state.value.canStart) return
        updateState { copy(phase = InterviewPhase.INTERVIEWING, transcript = emptyList(), facts = initialFacts) }
        requestNext(force = false)
    }

    private suspend fun answer(text: String, wasCustom: Boolean) {
        val current = state.value
        val question = current.currentQuestion ?: return
        if (current.isLoading) return

        updateState {
            copy(
                transcript = transcript + AnsweredQuestion(question, text, wasCustom, factsBefore = facts),
                currentQuestion = null,
                customAnswer = ""
            )
        }
        requestNext(force = false)
    }

    /** Local rewind — restores the question and fact sheet exactly as they were. No API call. */
    private fun rewindTo(index: Int) {
        val current = state.value
        val target = current.transcript.getOrNull(index) ?: return
        if (current.isLoading) return
        updateState {
            copy(
                phase = InterviewPhase.INTERVIEWING,
                transcript = transcript.take(index),
                currentQuestion = target.question,
                facts = target.factsBefore,
                customAnswer = if (target.wasCustom) target.answer else "",
                recommendation = null,
                turnNumber = index + 1,
                error = null
            )
        }
    }

    private suspend fun requestNext(force: Boolean) {
        val snapshot = state.value
        updateState { copy(isLoading = true, error = null, pendingForceRecommendation = force) }

        try {
            val step = nextInterviewStep(
                seed = snapshot.seed,
                history = snapshot.transcript.map { it.toTurn() },
                facts = snapshot.facts,
                forceRecommendation = force
            )
            updateState {
                when (step) {
                    is InterviewStep.Question -> copy(
                        isLoading = false,
                        currentQuestion = step.question,
                        facts = step.facts,
                        turnNumber = step.turnNumber,
                        maxTurns = step.maxTurns
                    )
                    is InterviewStep.Recommendation -> copy(
                        isLoading = false,
                        phase = InterviewPhase.DONE,
                        currentQuestion = null,
                        recommendation = step.recommendation,
                        facts = step.facts,
                        turnNumber = step.turnNumber,
                        maxTurns = step.maxTurns
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            updateState {
                copy(
                    isLoading = false,
                    error = e.message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Tap Retry."
                )
            }
        }
    }

    private suspend fun saveMatter() {
        val current = state.value
        if (!current.canSaveMatter) return
        updateState { copy(isSavingMatter = true) }
        try {
            val id = saveInterviewResult(
                existingId = current.existingCaseId,
                seed = current.seed,
                facts = current.facts,
                recommendation = current.recommendation
            )
            updateState { copy(isSavingMatter = false) }
            sendEffect(InterviewEffect.MatterSaved(id, wasUpdate = current.existingCaseId != null))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            updateState { copy(isSavingMatter = false) }
            sendEffect(InterviewEffect.ShowMessage(e.message ?: "Couldn't save the matter."))
        }
    }

    companion object {
        const val ARG_CASE_ID = "caseId"
    }
}
