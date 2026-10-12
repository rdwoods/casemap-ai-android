package com.casemapai.ui.home

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter

data class HomeState(
    val isLoading: Boolean = true,
    val matters: List<GetMatterQuery.Data.Matter> = emptyList(),
    val error: String? = null
)

sealed interface HomeIntent {
    data object Load : HomeIntent
}

sealed interface HomeEffect
