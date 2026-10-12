package com.casemapai.ui.matter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMatterScreen(
    viewModel: EditMatterViewModel,
    onSaved: (UUID) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is EditMatterEffect.Saved -> onSaved(effect.matterId)
                is EditMatterEffect.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Matter") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ElevatedCard {
                Text(
                    text = "Do not include names, A-numbers, dates of birth, addresses, or other identifying information.",
                    modifier = Modifier.padding(16.dp)
                )
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(EditMatterIntent.TitleChanged(it)) },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            MultiLineField(
                value = state.summary,
                onValueChange = { viewModel.onIntent(EditMatterIntent.SummaryChanged(it)) },
                label = "Sanitized Summary"
            )

            MultiLineField(
                value = state.proceduralPosture,
                onValueChange = { viewModel.onIntent(EditMatterIntent.ProceduralPostureChanged(it)) },
                label = "Procedural Posture"
            )

            MultiLineField(
                value = state.disputedFacts,
                onValueChange = { viewModel.onIntent(EditMatterIntent.DisputedFactsChanged(it)) },
                label = "Disputed Facts"
            )

            MultiLineField(
                value = state.notes,
                onValueChange = { viewModel.onIntent(EditMatterIntent.NotesChanged(it)) },
                label = "Notes"
            )

            Button(
                onClick = { viewModel.onIntent(EditMatterIntent.SaveClicked) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving
            ) {
                Text(if (state.isSaving) "Saving..." else "Save Matter")
            }
        }
    }
}

@Composable
private fun MultiLineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 4
    )
}
