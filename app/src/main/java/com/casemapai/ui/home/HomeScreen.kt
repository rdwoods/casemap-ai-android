package com.casemapai.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.casemapai.domain.model.casefile.CaseFile
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartInterview: () -> Unit,
    onOpenMatter: (UUID) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("CaseMap AI") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onStartInterview) { Text("New matter") }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(state.error!!)
            }
            state.matters.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text("No matters yet. Tap New matter to start with a short interview instead of a long form.")
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.matters, key = { it.id }) { matter ->
                    MatterCard(matter = matter, onClick = { onOpenMatter(matter.id) })
                }
            }
        }
    }
}

@Composable
private fun MatterCard(
    matter: CaseFile,
    onClick: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(matter.title, style = MaterialTheme.typography.titleMedium)
            Text(matter.seed.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            val updated = DateUtils.getRelativeTimeSpanString(matter.updatedAt).toString()
            Text(
                "${matter.seed.caseType} · ${matter.facts.size} facts · Updated $updated",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
