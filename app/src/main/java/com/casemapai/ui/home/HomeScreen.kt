package com.casemapai.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCreateMatter: () -> Unit,
    onStartInterview: () -> Unit,
    onOpenMatter: (UUID) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CaseMap AI") },
                actions = {
                    TextButton(onClick = onStartInterview) { Text("Interview") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateMatter) {
                Text("+")
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    CircularProgressIndicator()
                }
            }
            state.matters.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                    Text("No matters yet. Tap + to create your first sanitized matter.")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.matters) { matter ->
                        MatterCard(matter = matter, onClick = { onOpenMatter(matter.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MatterCard(
    matter: GetMatterQuery.Data.Matter,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = matter.title, style = MaterialTheme.typography.titleMedium)
            Text(text = matter.summary, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            Text(text = "Updated: ${matter.updatedAt}", style = MaterialTheme.typography.labelMedium)
        }
    }
}
