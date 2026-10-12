package com.casemapai.domain.repository

import com.casemapai.domain.model.casefile.CaseFile
import com.casemapai.domain.model.interview.CaseFact
import com.casemapai.domain.model.interview.InterviewSeed
import com.casemapai.domain.model.interview.StrategyRecommendation
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Storage for saved matters. Today it's backed by an in-memory store; once the Data Connect
 * Kotlin SDK is generated from /dataconnect, add a DataConnectCaseFileRepository and change
 * one binding in CaseFileModule. Nothing above this interface changes.
 */
interface CaseFileRepository {

    fun observeCaseFiles(): Flow<List<CaseFile>>

    fun observeCaseFile(id: UUID): Flow<CaseFile?>

    suspend fun getCaseFile(id: UUID): CaseFile?

    /** New matter from a finished interview. Returns its id. */
    suspend fun createFromInterview(
        seed: InterviewSeed,
        facts: List<CaseFact>,
        recommendation: StrategyRecommendation?
    ): UUID

    /** A continued interview finished: merge facts (attorney edits win) and replace the recommendation. */
    suspend fun applyInterviewResult(
        id: UUID,
        facts: List<CaseFact>,
        recommendation: StrategyRecommendation?
    )

    suspend fun updateHeader(id: UUID, title: String, posture: String, summary: String)

    /** [factId] null = add a new fact. Either way the fact becomes ATTORNEY-sourced. */
    suspend fun upsertFact(id: UUID, factId: UUID?, label: String, value: String)

    suspend fun deleteFact(id: UUID, factId: UUID)
}
