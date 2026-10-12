package com.casemapai.ui.matter

import java.util.UUID

data class EditMatterState(
    val title: String = "",
    val summary: String = "",
    val proceduralPosture: String = "",
    val disputedFacts: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface EditMatterIntent {
    data class TitleChanged(val value: String) : EditMatterIntent
    data class SummaryChanged(val value: String) : EditMatterIntent
    data class ProceduralPostureChanged(val value: String) : EditMatterIntent
    data class DisputedFactsChanged(val value: String) : EditMatterIntent
    data class NotesChanged(val value: String) : EditMatterIntent

    data object SaveClicked : EditMatterIntent
}

sealed interface EditMatterEffect {
    data class Saved(val matterId: UUID) : EditMatterEffect
    data class ShowError(val message: String) : EditMatterEffect
}
