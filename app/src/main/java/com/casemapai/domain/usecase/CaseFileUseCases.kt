package com.casemapai.domain.usecase

import com.casemapai.domain.model.casefile.CaseFile
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.StrategyRecommendation
import com.casemapai.domain.repository.CaseFileRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class ObserveCaseFilesUseCase @Inject constructor(private val repository: CaseFileRepository) {
    operator fun invoke(): Flow<List<CaseFile>> = repository.observeCaseFiles()
}

class ObserveCaseFileUseCase @Inject constructor(private val repository: CaseFileRepository) {
    operator fun invoke(id: UUID): Flow<CaseFile?> = repository.observeCaseFile(id)
}

class GetCaseFileUseCase @Inject constructor(private val repository: CaseFileRepository) {
    suspend operator fun invoke(id: UUID): CaseFile? = repository.getCaseFile(id)
}

/** Saves a finished interview: creates a new matter, or merges into [existingId] for a continued one. */
class SaveInterviewResultUseCase @Inject constructor(private val repository: CaseFileRepository) {
    suspend operator fun invoke(
        existingId: UUID?,
        seed: InterviewSeed,
        facts: List<CaseFact>,
        recommendation: StrategyRecommendation?
    ): UUID {
        require(seed.isValid) { "Case type and summary are required." }
        return if (existingId == null) {
            repository.createFromInterview(seed, facts, recommendation)
        } else {
            repository.applyInterviewResult(existingId, facts, recommendation)
            existingId
        }
    }
}

class EditFactUseCase @Inject constructor(private val repository: CaseFileRepository) {
    suspend fun save(caseId: UUID, factId: UUID?, label: String, value: String) {
        require(label.isNotBlank() && value.isNotBlank()) { "Label and value are required." }
        repository.upsertFact(caseId, factId, label, value)
    }

    suspend fun delete(caseId: UUID, factId: UUID) = repository.deleteFact(caseId, factId)
}

class UpdateCaseHeaderUseCase @Inject constructor(private val repository: CaseFileRepository) {
    suspend operator fun invoke(caseId: UUID, title: String, posture: String, summary: String) {
        require(title.isNotBlank() && summary.isNotBlank()) { "Title and summary are required." }
        repository.updateHeader(caseId, title, posture, summary)
    }
}
