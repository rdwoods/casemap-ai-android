package com.casemapai.domain.model.casefile

import com.casemapai.domain.model.interview.CaseFact
import java.util.UUID

/**
 * Combines a matter's existing facts with a fresh fact sheet from the interview.
 *
 * Rules:
 *  - Every ATTORNEY fact is kept exactly as the attorney left it.
 *  - An incoming AI fact whose label matches an ATTORNEY fact (case-insensitive) is dropped;
 *    the attorney's version wins.
 *  - All other incoming facts replace the old AI facts.
 *  - Order: attorney facts keep their place; new AI facts follow in the model's order.
 */
object FactMerge {

    fun merge(
        existing: List<MatterFact>,
        incoming: List<CaseFact>,
        newId: () -> UUID = UUID::randomUUID
    ): List<MatterFact> {
        val attorneyFacts = existing.filter { it.source == FactSource.ATTORNEY }
        val attorneyLabels = attorneyFacts.map { it.label.normalized() }.toSet()

        // Reuse the old id when an AI fact keeps the same label, so UI rows don't flicker.
        val oldAiIdsByLabel = existing
            .filter { it.source == FactSource.AI }
            .associate { it.label.normalized() to it.id }

        val aiFacts = incoming
            .filter { it.label.isNotBlank() && it.value.isNotBlank() }
            .filterNot { it.label.normalized() in attorneyLabels }
            .distinctBy { it.label.normalized() }
            .map { fact ->
                MatterFact(
                    id = oldAiIdsByLabel[fact.label.normalized()] ?: newId(),
                    label = fact.label.trim(),
                    value = fact.value.trim(),
                    source = FactSource.AI
                )
            }

        return attorneyFacts + aiFacts
    }

    private fun String.normalized() = trim().lowercase()
}
