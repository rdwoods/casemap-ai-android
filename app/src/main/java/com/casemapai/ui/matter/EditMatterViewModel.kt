package com.casemapai.ui.matter

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.dataconnect.GetUserQuery
import com.casemapai.domain.usecase.SaveMatterUseCase
import com.casemapai.ui.mvi.BaseViewModel
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditMatterViewModel @Inject constructor(
    private val saveMatterUseCase: SaveMatterUseCase,
    private val currentUser: GetUserQuery.Data.User
) : BaseViewModel<EditMatterIntent, EditMatterState, EditMatterEffect>(EditMatterState()) {

    override suspend fun reduce(intent: EditMatterIntent) {
        when (intent) {
            is EditMatterIntent.TitleChanged -> updateState { copy(title = intent.value) }
            is EditMatterIntent.SummaryChanged -> updateState { copy(summary = intent.value) }
            is EditMatterIntent.ProceduralPostureChanged -> updateState { copy(proceduralPosture = intent.value) }
            is EditMatterIntent.DisputedFactsChanged -> updateState { copy(disputedFacts = intent.value) }
            is EditMatterIntent.NotesChanged -> updateState { copy(notes = intent.value) }

            EditMatterIntent.SaveClicked -> save()
        }
    }

    private suspend fun save() {
        val current = state.value
        if (current.title.isBlank() || current.summary.isBlank()) {
            sendEffect(EditMatterEffect.ShowError("Title and sanitized summary are required."))
            return
        }

        updateState { copy(isSaving = true, error = null) }

        val matterId = saveMatterUseCase(
            GetMatterQuery.Data.Matter(
                user = currentUser,
                id = currentUser.id,
                title = current.title.trim(),
                summary = current.summary.trim(),
                proceduralPosture = current.proceduralPosture.trim(),
                disputedFacts = current.disputedFacts.trim(),
                notes = current.notes.trim(),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )
        )

        updateState { copy(isSaving = false) }
        sendEffect(EditMatterEffect.Saved(matterId))
    }
}