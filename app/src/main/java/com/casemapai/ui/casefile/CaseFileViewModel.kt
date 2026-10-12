package com.casemapai.ui.casefile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.casemapai.domain.usecase.EditFactUseCase
import com.casemapai.domain.usecase.ObserveCaseFileUseCase
import com.casemapai.domain.usecase.UpdateCaseHeaderUseCase
import com.casemapai.ui.mvi.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CaseFileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCaseFile: ObserveCaseFileUseCase,
    private val editFact: EditFactUseCase,
    private val updateHeader: UpdateCaseHeaderUseCase
) : BaseViewModel<CaseFileIntent, CaseFileState, CaseFileEffect>(CaseFileState()) {

    // Route args are strings; parsing here avoids the Long/UUID mismatch in the old matter route.
    private val caseId: UUID = UUID.fromString(checkNotNull(savedStateHandle.get<String>(ARG_CASE_ID)))

    init {
        viewModelScope.launch {
            observeCaseFile(caseId).collect { file ->
                updateState {
                    copy(
                        isLoading = false,
                        caseFile = file,
                        error = if (file == null) "This matter no longer exists." else null
                    )
                }
            }
        }
    }

    override suspend fun reduce(intent: CaseFileIntent) {
        when (intent) {
            is CaseFileIntent.OpenFact -> {
                val fact = state.value.caseFile?.facts?.firstOrNull { it.id == intent.factId } ?: return
                updateState { copy(sheet = EditorSheet.Fact(fact.id, fact.label, fact.value)) }
            }
            CaseFileIntent.AddFact -> updateState { copy(sheet = EditorSheet.Fact(null, "", "")) }
            CaseFileIntent.EditHeader -> {
                val file = state.value.caseFile ?: return
                updateState { copy(sheet = EditorSheet.Header(file.title, file.seed.posture, file.seed.summary)) }
            }
            is CaseFileIntent.SheetFieldChanged -> updateState { copy(sheet = sheet?.withField(intent.field, intent.value)) }
            CaseFileIntent.DismissSheet -> if (!state.value.isSaving) updateState { copy(sheet = null) }
            CaseFileIntent.SaveSheet -> saveSheet()
            CaseFileIntent.DeleteFactInSheet -> deleteFactInSheet()
            is CaseFileIntent.RestoreFact -> runSafely {
                editFact.save(caseId, intent.fact.id, intent.fact.label, intent.fact.value)
            }
            CaseFileIntent.ContinueInterview -> sendEffect(CaseFileEffect.OpenInterview(caseId))
        }
    }

    private suspend fun saveSheet() {
        val sheet = state.value.sheet ?: return
        val ok = runSafely {
            when (sheet) {
                is EditorSheet.Fact -> {
                    if (!sheet.canSave) return@runSafely
                    editFact.save(caseId, sheet.factId, sheet.label, sheet.value)
                }
                is EditorSheet.Header -> {
                    if (!sheet.canSave) return@runSafely
                    updateHeader(caseId, sheet.title, sheet.posture, sheet.summary)
                }
            }
        }
        if (ok) updateState { copy(sheet = null) }
    }

    private suspend fun deleteFactInSheet() {
        val sheet = state.value.sheet as? EditorSheet.Fact ?: return
        val factId = sheet.factId ?: return updateState { copy(sheet = null) }
        val deleted = state.value.caseFile?.facts?.firstOrNull { it.id == factId } ?: return
        if (runSafely { editFact.delete(caseId, factId) }) {
            updateState { copy(sheet = null) }
            sendEffect(CaseFileEffect.FactDeleted(deleted))
        }
    }

    /** Returns true on success; on failure keeps the sheet open and reports the error. */
    private suspend fun runSafely(block: suspend () -> Unit): Boolean {
        updateState { copy(isSaving = true) }
        return try {
            block()
            updateState { copy(isSaving = false) }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            updateState { copy(isSaving = false) }
            sendEffect(CaseFileEffect.ShowMessage(e.message ?: "Couldn't save. Try again."))
            false
        }
    }

    private fun EditorSheet.withField(field: SheetField, value: String): EditorSheet = when (this) {
        is EditorSheet.Fact -> when (field) {
            SheetField.LABEL -> copy(label = value)
            SheetField.VALUE -> copy(value = value)
            else -> this
        }
        is EditorSheet.Header -> when (field) {
            SheetField.TITLE -> copy(title = value)
            SheetField.POSTURE -> copy(posture = value)
            SheetField.SUMMARY -> copy(summary = value)
            else -> this
        }
    }

    companion object {
        const val ARG_CASE_ID = "caseId"
    }
}
