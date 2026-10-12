package com.casemapai.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.casemapai.domain.model.interview.StrategyRecommendation

/** Shared by the interview result and the saved matter screen. */
@Composable
fun RecommendationCard(rec: StrategyRecommendation) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Suggested approach", style = MaterialTheme.typography.titleLarge)
            Text(rec.summary, style = MaterialTheme.typography.bodyLarge)

            rec.approaches.forEachIndexed { i, approach ->
                HorizontalDivider()
                Text("${i + 1}. ${approach.title}", style = MaterialTheme.typography.titleMedium)
                Text(approach.rationale, style = MaterialTheme.typography.bodyMedium)
                if (approach.risks.isNotBlank()) {
                    Text(
                        "Risk: ${approach.risks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (rec.missingFacts.isNotEmpty()) {
                HorizontalDivider()
                BulletSection("Facts that could change this", rec.missingFacts)
            }

            if (rec.verifyBeforeRelying.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(12.dp)) {
                        BulletSection("Verify before relying on this", rec.verifyBeforeRelying)
                    }
                }
            }

            Text(
                "AI-generated brainstorming. Not legal advice; verify all authority independently.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun BulletSection(title: String, items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        items.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
    }
}
