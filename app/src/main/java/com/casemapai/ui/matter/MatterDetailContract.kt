package com.casemapai.ui.matter

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter

data class MatterDetailState(
    val isLoading: Boolean = true,
    val isRunningAnalysis: Boolean = false,
    val matter: GetMatterQuery.Data.Matter? = null,
    val error: String? = null
)

sealed interface MatterDetailIntent {
    data object Load : MatterDetailIntent
    data object RunIssueSpotterClicked : MatterDetailIntent
}

sealed interface MatterDetailEffect {
    data object OpenAnalysis : MatterDetailEffect
    data class ShowError(val message: String) : MatterDetailEffect
}
