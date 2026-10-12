package com.casemapai.di

import com.casemapai.dataconnect.CasemapaiConnector
import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.dataconnect.GetUserQuery
import com.casemapai.dataconnect.ListAnalysisResultsQuery
import com.casemapai.dataconnect.ListMattersQuery
import com.google.firebase.Timestamp
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SqlConnectDataSource @Inject constructor() {

    private val connector = CasemapaiConnector.instance

    // ── User ──────────────────────────────────────────────────────────────

    suspend fun getUser(id: UUID): GetUserQuery.Data.User? =
        connector.getUser.execute(id = id).data.user

    // ── Matters ───────────────────────────────────────────────────────────

    suspend fun listMatters(userId: UUID): List<ListMattersQuery.Data.MattersItem> =
        connector.listMatters.execute(userId = userId).data.matters

    suspend fun getMatter(id: UUID): GetMatterQuery.Data.Matter? =
        connector.getMatter.execute(id = id).data.matter


    suspend fun createMatter(userId: UUID, title: String, summary: String, proceduralPosture: String, disputedFacts: String): UUID{
        val result = connector.createMatter.execute(
            userId = userId,
            title = title,
            summary = summary,
            proceduralPosture = proceduralPosture,
            disputedFacts = disputedFacts,
            createdAt = Timestamp(Date(System.currentTimeMillis())),
            updatedAt = Timestamp(Date(System.currentTimeMillis())))
            .data
            .matter_insert
            .id

        return result
    }

    // ── Analysis Results ──────────────────────────────────────────────────

    suspend fun listAnalysisResults(
        matterId: UUID
    ): List<ListAnalysisResultsQuery.Data.AnalysisResultsItem> =
        connector.listAnalysisResults.execute(matterId = matterId).data.analysisResults
}
