package com.casemapai.domain.model.casefile

import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.StrategyRecommendation
import java.util.UUID

/** Who last set a fact. Attorney edits are authoritative and survive later interviews. */
enum class FactSource { AI, ATTORNEY }

data class MatterFact(
    val id: UUID,
    val label: String,
    val value: String,
    val source: FactSource
)

/**
 * A saved matter: a short header plus labeled facts and the latest recommendation.
 * Named CaseFile to avoid colliding with the older Matter / GetMatterQuery types
 * while both exist; rename once the old form-based screens are deleted.
 */
data class CaseFile(
    val id: UUID,
    val title: String,
    val seed: InterviewSeed,
    val facts: List<MatterFact>,
    val recommendation: StrategyRecommendation?,
    val updatedAt: Long
) {
    /** The facts in the shape the interview expects, for "Continue interview". */
    fun factsForInterview(): List<CaseFact> = facts.map { CaseFact(it.label, it.value) }
}
