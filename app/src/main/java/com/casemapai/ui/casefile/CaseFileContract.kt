package com.casemapai.ui.casefile

import com.casemapai.domain.model.casefile.CaseFile
import com.casemapai.domain.model.casefile.MatterFact
import java.util.UUID

/** The single bottom sheet that's open, if any. Editing happens one small thing at a time. */
sealed interface EditorSheet {
    data class Fact(
        val factId: UUID?,
        val label: String,
        val value: String
    ) : EditorSheet {
        val isNew: Boolean get() = factId == null
        val canSave: Boolean get() = label.isNotBlank() && value.isNotBlank()
    }

    data class Header(
        val title: String,
        val posture: String,
        val summary: String
    ) : EditorSheet {
        val canSave: Boolean get() = title.isNotBlank() && summary.isNotBlank()
    }
}

enum class SheetField { LABEL, VALUE, TITLE, POSTURE, SUMMARY }

data class CaseFileState(
    val isLoading: Boolean = true,
    val caseFile: CaseFile? = null,
    val sheet: EditorSheet? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface CaseFileIntent {
    data class OpenFact(val factId: UUID) : CaseFileIntent
    data object AddFact : CaseFileIntent
    data object EditHeader : CaseFileIntent
    data class SheetFieldChanged(val field: SheetField, val value: String) : CaseFileIntent
    data object SaveSheet : CaseFileIntent
    data object DeleteFactInSheet : CaseFileIntent
    data object DismissSheet : CaseFileIntent
    data class RestoreFact(val fact: MatterFact) : CaseFileIntent
    data object ContinueInterview : CaseFileIntent
}

sealed interface CaseFileEffect {
    data class OpenInterview(val caseId: UUID) : CaseFileEffect
    data class FactDeleted(val fact: MatterFact) : CaseFileEffect
    data class ShowMessage(val message: String) : CaseFileEffect
}
