package com.casemapai.data.repository

import com.casemapai.domain.model.casefile.CaseFile
import com.casemapai.domain.model.casefile.FactMerge
import com.casemapai.domain.model.casefile.FactSource
import com.casemapai.domain.model.casefile.MatterFact
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.StrategyRecommendation
import com.casemapai.domain.repository.CaseFileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-lifetime store so the whole flow works before Data Connect is deployed.
 * Data is lost when the app process dies — that's expected for now.
 */
@Singleton
class InMemoryCaseFileRepository @Inject constructor() : CaseFileRepository {

    private val store = MutableStateFlow<Map<UUID, CaseFile>>(emptyMap())

    /** Overridable in tests for deterministic timestamps. */
    var clock: () -> Long = System::currentTimeMillis

    override fun observeCaseFiles(): Flow<List<CaseFile>> =
        store.map { all -> all.values.sortedByDescending { it.updatedAt } }

    override fun observeCaseFile(id: UUID): Flow<CaseFile?> =
        store.map { it[id] }.distinctUntilChanged()

    override suspend fun getCaseFile(id: UUID): CaseFile? = store.value[id]

    override suspend fun createFromInterview(
        seed: InterviewSeed,
        facts: List<CaseFact>,
        recommendation: StrategyRecommendation?
    ): UUID {
        val id = UUID.randomUUID()
        val caseFile = CaseFile(
            id = id,
            title = seed.caseType.ifBlank { "Untitled matter" },
            seed = seed,
            facts = FactMerge.merge(existing = emptyList(), incoming = facts),
            recommendation = recommendation,
            updatedAt = clock()
        )
        store.update { it + (id to caseFile) }
        return id
    }

    override suspend fun applyInterviewResult(
        id: UUID,
        facts: List<CaseFact>,
        recommendation: StrategyRecommendation?
    ) = edit(id) { file ->
        file.copy(
            facts = FactMerge.merge(existing = file.facts, incoming = facts),
            recommendation = recommendation ?: file.recommendation
        )
    }

    override suspend fun updateHeader(id: UUID, title: String, posture: String, summary: String) = edit(id) { file ->
        file.copy(
            title = title.trim().ifBlank { file.title },
            seed = file.seed.copy(posture = posture.trim(), summary = summary.trim().ifBlank { file.seed.summary })
        )
    }

    override suspend fun upsertFact(id: UUID, factId: UUID?, label: String, value: String) = edit(id) { file ->
        val edited = MatterFact(
            id = factId ?: UUID.randomUUID(),
            label = label.trim(),
            value = value.trim(),
            source = FactSource.ATTORNEY
        )
        val exists = factId != null && file.facts.any { it.id == factId }
        file.copy(
            facts = if (exists) file.facts.map { if (it.id == factId) edited else it } else file.facts + edited
        )
    }

    override suspend fun deleteFact(id: UUID, factId: UUID) = edit(id) { file ->
        file.copy(facts = file.facts.filterNot { it.id == factId })
    }

    private fun edit(id: UUID, transform: (CaseFile) -> CaseFile) {
        store.update { all ->
            val current = all[id] ?: throw NoSuchElementException("Matter not found.")
            all + (id to transform(current).copy(updatedAt = clock()))
        }
    }
}
