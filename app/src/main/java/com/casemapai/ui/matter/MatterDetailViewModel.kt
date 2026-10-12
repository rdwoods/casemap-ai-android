package com.casemapai.ui.matter

import androidx.lifecycle.SavedStateHandle
import com.casemapai.domain.usecase.GetMatterUseCase
import com.casemapai.domain.usecase.RunIssueSpotterUseCase
import com.casemapai.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MatterDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMatterUseCase: GetMatterUseCase,
    private val runIssueSpotterUseCase: RunIssueSpotterUseCase
) : BaseViewModel<MatterDetailIntent, MatterDetailState, MatterDetailEffect>(MatterDetailState()) {

    private val matterId: UUID = checkNotNull(savedStateHandle["matterId"])

    init {
        onIntent(MatterDetailIntent.Load)
    }

    override suspend fun reduce(intent: MatterDetailIntent) {
        when (intent) {
            MatterDetailIntent.Load -> load()
            MatterDetailIntent.RunIssueSpotterClicked -> runAnalysis()
        }
    }

    private suspend fun load() {
        updateState { copy(isLoading = true, error = null) }
        val matter = getMatterUseCase(matterId)
        updateState {
            copy(
                isLoading = false,
                matter = matter,
                error = if (matter == null) "Matter not found." else null
            )
        }
    }

    private suspend fun runAnalysis() {
        val matter = state.value.matter ?: run {
            sendEffect(MatterDetailEffect.ShowError("Matter not loaded."))
            return
        }

        updateState { copy(isRunningAnalysis = true) }
        launchCoroutine(getExceptionHandler { e -> handleRunAnalysisException(e) }){
            val response = runIssueSpotterUseCase(matter = matter)

            updateState { copy(isRunningAnalysis = false) }
            sendEffect(MatterDetailEffect.OpenAnalysis)
        }

    }

    private fun handleRunAnalysisException(e: Throwable) {
        launchExceptionCoroutine {
            updateState { copy(isLoading = false, error = "Errors") }
        }
    }
}