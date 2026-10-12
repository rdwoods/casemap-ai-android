package com.casemapai.ui.casefile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.casemapai.domain.model.casefile.CaseFile
import com.casemapai.domain.model.casefile.FactSource
import com.casemapai.domain.model.casefile.MatterFact
import com.casemapai.ui.common.RecommendationCard
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseFileScreen(
    viewModel: CaseFileViewModel,
    onBack: () -> Unit,
    onContinueInterview: (UUID) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CaseFileEffect.OpenInterview -> onContinueInterview(effect.caseId)
                is CaseFileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is CaseFileEffect.FactDeleted -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Deleted \"${effect.fact.label}\"",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.onIntent(CaseFileIntent.RestoreFact(effect.fact))
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.caseFile?.title ?: "Matter", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val file = state.caseFile
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            file == null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(state.error ?: "Unable to load matter.")
            }
            else -> CaseFileContent(
                file = file,
                contentPadding = padding,
                onIntent = viewModel::onIntent
            )
        }
    }

    state.sheet?.let { sheet ->
        EditorBottomSheet(
            sheet = sheet,
            isSaving = state.isSaving,
            onIntent = viewModel::onIntent
        )
    }
}

@Composable
private fun CaseFileContent(
    file: CaseFile,
    contentPadding: PaddingValues,
    onIntent: (CaseFileIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { HeaderCard(file) { onIntent(CaseFileIntent.EditHeader) } }

        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Case facts (${file.facts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onIntent(CaseFileIntent.AddFact) }) { Text("Add fact") }
            }
        }

        if (file.facts.isEmpty()) {
            item {
                Text(
                    "No facts yet. Continue the interview or add one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column {
                        file.facts.forEachIndexed { index, fact ->
                            if (index > 0) HorizontalDivider()
                            FactRow(fact) { onIntent(CaseFileIntent.OpenFact(fact.id)) }
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = { onIntent(CaseFileIntent.ContinueInterview) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (file.recommendation == null) "Start interview" else "Continue interview") }
        }

        file.recommendation?.let { rec -> item { RecommendationCard(rec) } }
    }
}

@Composable
private fun HeaderCard(file: CaseFile, onEdit: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Edit matter header", onClick = onEdit)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(file.seed.caseType, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            if (file.seed.posture.isNotBlank()) {
                Text(file.seed.posture, style = MaterialTheme.typography.bodySmall)
            }
            Text(file.seed.summary, style = MaterialTheme.typography.bodyMedium)
            Text(
                "Tap to edit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FactRow(fact: MatterFact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Edit ${fact.label}", onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(fact.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(fact.value, style = MaterialTheme.typography.bodyLarge)
        }
        if (fact.source == FactSource.ATTORNEY) {
            SuggestionChip(onClick = onClick, label = { Text("Edited") })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorBottomSheet(
    sheet: EditorSheet,
    isSaving: Boolean,
    onIntent: (CaseFileIntent) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { onIntent(CaseFileIntent.DismissSheet) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (sheet) {
                is EditorSheet.Fact -> {
                    Text(if (sheet.isNew) "Add fact" else "Edit fact", style = MaterialTheme.typography.titleMedium)
                    SheetTextField("Label", sheet.label, singleLine = true) {
                        onIntent(CaseFileIntent.SheetFieldChanged(SheetField.LABEL, it))
                    }
                    SheetTextField("Value", sheet.value, minLines = 2) {
                        onIntent(CaseFileIntent.SheetFieldChanged(SheetField.VALUE, it))
                    }
                    Text(
                        "Your edits are kept even if you continue the interview later.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SheetActions(
                        canSave = sheet.canSave && !isSaving,
                        showDelete = !sheet.isNew,
                        isSaving = isSaving,
                        onIntent = onIntent
                    )
                }
                is EditorSheet.Header -> {
                    Text("Edit matter", style = MaterialTheme.typography.titleMedium)
                    SheetTextField("Title", sheet.title, singleLine = true) {
                        onIntent(CaseFileIntent.SheetFieldChanged(SheetField.TITLE, it))
                    }
                    SheetTextField("Procedural posture", sheet.posture, singleLine = true) {
                        onIntent(CaseFileIntent.SheetFieldChanged(SheetField.POSTURE, it))
                    }
                    SheetTextField("Sanitized summary", sheet.summary, minLines = 3) {
                        onIntent(CaseFileIntent.SheetFieldChanged(SheetField.SUMMARY, it))
                    }
                    SheetActions(
                        canSave = sheet.canSave && !isSaving,
                        showDelete = false,
                        isSaving = isSaving,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetTextField(
    label: String,
    value: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SheetActions(
    canSave: Boolean,
    showDelete: Boolean,
    isSaving: Boolean,
    onIntent: (CaseFileIntent) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (showDelete) {
            OutlinedButton(
                onClick = { onIntent(CaseFileIntent.DeleteFactInSheet) },
                enabled = !isSaving
            ) { Text("Delete") }
        }
        Box(Modifier.weight(1f))
        TextButton(onClick = { onIntent(CaseFileIntent.DismissSheet) }, enabled = !isSaving) { Text("Cancel") }
        Button(onClick = { onIntent(CaseFileIntent.SaveSheet) }, enabled = canSave) {
            Text(if (isSaving) "Saving…" else "Save")
        }
    }
}
