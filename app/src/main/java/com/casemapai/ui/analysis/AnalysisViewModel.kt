package com.casemapai.ui.analysis

import androidx.lifecycle.SavedStateHandle
import com.casemapai.domain.model.Matter
import com.casemapai.domain.usecase.GetMatterUseCase
import com.casemapai.domain.usecase.RunIssueSpotterUseCase
import com.casemapai.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMatterUseCase: GetMatterUseCase,
    private val runIssueSpotterUseCase: RunIssueSpotterUseCase
) : BaseViewModel<AnalysisIntent, AnalysisState, AnalysisEffect>(AnalysisState()) {

    private val matterId: UUID = checkNotNull(savedStateHandle["matterId"])

    init {
        onIntent(AnalysisIntent.Load)
    }

    override suspend fun reduce(intent: AnalysisIntent) {
        when (intent) {
            AnalysisIntent.Load -> load()
        }
    }

    private suspend fun load() {
        updateState { copy(isLoading = true, error = null) }
        val matter = getMatterUseCase(matterId)
        if (matter == null) {
            updateState { copy(isLoading = false, error = "Matter not found.") }
            return
        }

        val result = runIssueSpotterUseCase(matter)
        updateState {
            copy(
                isLoading = false,
                resultBody = result.body
            )
        }
    }
}