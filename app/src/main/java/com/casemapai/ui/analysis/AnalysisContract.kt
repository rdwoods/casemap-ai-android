package com.casemapai.ui.analysis

import com.casemapai.ui.home.HomeIntent

data class AnalysisState(
    val isLoading: Boolean = true,
    val resultBody: String = "",
    val error: String? = null
)

sealed interface AnalysisIntent {
    data object Load : AnalysisIntent
}

sealed interface AnalysisEffect
