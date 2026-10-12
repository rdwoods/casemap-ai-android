package com.casemapai.ui.interview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.casemapai.data.sample.SampleScenarios
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewQuestion
import com.casemapai.ui.common.RecommendationCard
import com.casemapai.ui.theme.CaseMapTheme
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    viewModel: InterviewViewModel,
    onBack: () -> Unit,
    onMatterSaved: (caseId: UUID, wasUpdate: Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InterviewEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                is InterviewEffect.MatterSaved -> onMatterSaved(effect.caseId, effect.wasUpdate)
            }
        }
    }

    InterviewScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onIntent = viewModel::onIntent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    state: InterviewState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onIntent: (InterviewIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isContinuing) "Continue Interview" else "Strategy Interview") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
                actions = {
                    if (state.phase != InterviewPhase.SEED) {
                        TextButton(
                            onClick = { onIntent(InterviewIntent.Restart) },
                            enabled = !state.isLoading
                        ) { Text("Restart") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
        ) {
            when (state.phase) {
                InterviewPhase.SEED -> SeedForm(state = state, onIntent = onIntent)
                InterviewPhase.INTERVIEWING,
                InterviewPhase.DONE -> InterviewConversation(state = state, onIntent = onIntent)
            }
        }
    }
}

// ---------------- Seed ----------------

@Composable
private fun SeedForm(
    state: InterviewState,
    onIntent: (InterviewIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Give the basics. The interview will ask for the rest.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        item {
            Text("Fictional test scenarios", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(SampleScenarios.all) { scenario ->
                    AssistChip(
                        onClick = { onIntent(InterviewIntent.LoadSample(scenario)) },
                        label = { Text(scenario.label) }
                    )
                }
            }
        }
        item {
            SeedTextField("Case type *", state.seed.caseType, singleLine = true) {
                onIntent(InterviewIntent.SeedChanged(SeedField.CASE_TYPE, it))
            }
        }
        item {
            SeedTextField("Procedural posture", state.seed.posture, singleLine = true) {
                onIntent(InterviewIntent.SeedChanged(SeedField.POSTURE, it))
            }
        }
        item {
            SeedTextField("Sanitized summary *", state.seed.summary, minLines = 4) {
                onIntent(InterviewIntent.SeedChanged(SeedField.SUMMARY, it))
            }
        }
        item {
            SeedTextField("Your goal (optional)", state.seed.goal, minLines = 2) {
                onIntent(InterviewIntent.SeedChanged(SeedField.GOAL, it))
            }
        }
        item {
            Text(
                "Use sanitized or fictional facts only. No names, A-numbers, or identifying details.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Button(
                onClick = { onIntent(InterviewIntent.Start) },
                enabled = state.canStart,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Start interview") }
        }
    }
}

@Composable
private fun SeedTextField(
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

// ---------------- Interview ----------------

@Composable
private fun InterviewConversation(
    state: InterviewState,
    onIntent: (InterviewIntent) -> Unit
) {
    val listState = rememberLazyListState()

    // Keep the newest question / result in view.
    LaunchedEffect(state.transcript.size, state.currentQuestion, state.recommendation, state.error) {
        val last = listState.layoutInfo.totalItemsCount - 1
        if (last >= 0) listState.animateScrollToItem(last)
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ProgressHeader(state) }

        itemsIndexed(state.transcript) { index, answered ->
            AnsweredQuestionRow(
                number = index + 1,
                question = answered.question.text,
                answer = answered.answer,
                wasCustom = answered.wasCustom,
                editEnabled = !state.isLoading,
                onEdit = { onIntent(InterviewIntent.EditAnswer(index)) }
            )
        }

        state.currentQuestion?.let { question ->
            item {
                CurrentQuestionCard(
                    number = state.transcript.size + 1,
                    question = question,
                    customAnswer = state.customAnswer,
                    enabled = !state.isLoading,
                    canSubmitCustom = state.canSubmitCustom,
                    onIntent = onIntent
                )
            }
        }

        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        state.error?.let { message ->
            item { ErrorCard(message) { onIntent(InterviewIntent.Retry) } }
        }

        state.recommendation?.let { rec ->
            item { RecommendationCard(rec) }
            item {
                Button(
                    onClick = { onIntent(InterviewIntent.SaveMatter) },
                    enabled = state.canSaveMatter,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            state.isSavingMatter -> "Saving…"
                            state.isContinuing -> "Update matter"
                            else -> "Save as matter"
                        }
                    )
                }
            }
        }

        if (state.facts.isNotEmpty()) {
            item { FactSheet(state.facts) }
        }

        if (state.canFinishEarly && state.currentQuestion != null) {
            item {
                TextButton(
                    onClick = { onIntent(InterviewIntent.FinishNow) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Skip remaining questions — recommend now") }
            }
        }
    }
}

@Composable
private fun ProgressHeader(state: InterviewState) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(state.existingTitle ?: state.seed.caseType, style = MaterialTheme.typography.titleMedium)
        if (state.isContinuing && state.transcript.isEmpty()) {
            Text(
                "Picking up from ${state.initialFacts.size} saved facts. Your edited facts stay as you left them.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        val label = if (state.phase == InterviewPhase.DONE) {
            "Done after ${state.transcript.size} questions"
        } else {
            "Question ${state.transcript.size + 1} of up to ${state.maxTurns}"
        }
        Text(label, style = MaterialTheme.typography.labelMedium)
        LinearProgressIndicator(
            progress = {
                if (state.phase == InterviewPhase.DONE) 1f
                else (state.transcript.size.toFloat() / state.maxTurns).coerceIn(0f, 1f)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AnsweredQuestionRow(
    number: Int,
    question: String,
    answer: String,
    wasCustom: Boolean,
    editEnabled: Boolean,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Q$number. $question", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (wasCustom) "✎ $answer" else answer,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEdit, enabled = editEnabled) { Text("Edit") }
            }
        }
    }
}

@Composable
private fun CurrentQuestionCard(
    number: Int,
    question: InterviewQuestion,
    customAnswer: String,
    enabled: Boolean,
    canSubmitCustom: Boolean,
    onIntent: (InterviewIntent) -> Unit
) {
    var showCustom by rememberSaveable(question.text) { mutableStateOf(customAnswer.isNotEmpty()) }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Q$number. ${question.text}", style = MaterialTheme.typography.titleMedium)
            if (question.why.isNotBlank()) {
                Text(
                    question.why,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            question.options.forEach { option ->
                OutlinedButton(
                    onClick = { onIntent(InterviewIntent.OptionSelected(option)) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(option, modifier = Modifier.fillMaxWidth()) }
            }

            if (!showCustom) {
                TextButton(onClick = { showCustom = true }, enabled = enabled) {
                    Text("None of these — answer in my own words")
                }
            } else {
                OutlinedTextField(
                    value = customAnswer,
                    onValueChange = { onIntent(InterviewIntent.CustomAnswerChanged(it)) },
                    label = { Text("Your answer") },
                    enabled = enabled,
                    minLines = 2,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { if (canSubmitCustom) onIntent(InterviewIntent.SubmitCustomAnswer) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { onIntent(InterviewIntent.SubmitCustomAnswer) },
                    enabled = canSubmitCustom,
                    modifier = Modifier.align(Alignment.End)
                ) { Text("Use my answer") }
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun FactSheet(facts: List<CaseFact>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide case facts (${facts.size})" else "Show case facts (${facts.size})")
            }
            if (expanded) {
                facts.forEach { fact ->
                    Text("${fact.label}: ${fact.value}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InterviewScreenPreview() {
    CaseMapTheme {
        InterviewScreen(
            state = InterviewState(
                seed = SampleScenarios.all[0].seed
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onIntent = {}
        )
    }
}
