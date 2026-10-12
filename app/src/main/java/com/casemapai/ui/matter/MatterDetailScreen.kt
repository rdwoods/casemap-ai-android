package com.casemapai.ui.matter

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatterDetailScreen(
    viewModel: MatterDetailViewModel,
    onBack: () -> Unit,
    onOpenAnalysis: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                MatterDetailEffect.OpenAnalysis -> onOpenAnalysis()
                is MatterDetailEffect.ShowError -> Unit
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matter Detail") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CircularProgressIndicator()
                }
            }
            state.matter == null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Text(state.error ?: "Unable to load matter.")
                }
            }
            else -> {
                val matter = state.matter
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(matter!!.title, style = MaterialTheme.typography.titleLarge)
                            Text("Summary: ${matter.summary}")
                            Text("Procedural Posture: ${matter.proceduralPosture}")
                            Text("Disputed Facts: ${matter.disputedFacts}")
                            Text("Notes: ${matter.notes}")
                        }
                    }

                    Button(
                        onClick = { viewModel.onIntent(MatterDetailIntent.RunIssueSpotterClicked) },
                        enabled = !state.isRunningAnalysis,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isRunningAnalysis) "Running..." else "Run Issue Spotter")
                    }
                }
            }
        }
    }
}
