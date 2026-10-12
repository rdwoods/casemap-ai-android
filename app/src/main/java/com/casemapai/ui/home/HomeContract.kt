package com.casemapai.ui.home

import com.casemapai.domain.model.casefile.CaseFile

data class HomeState(
    val isLoading: Boolean = true,
    val matters: List<CaseFile> = emptyList(),
    val error: String? = null
)

sealed interface HomeIntent {
    data object Load : HomeIntent
}

sealed interface HomeEffect
