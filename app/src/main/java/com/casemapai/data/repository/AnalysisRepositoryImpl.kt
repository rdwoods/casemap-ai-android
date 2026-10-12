package com.casemapai.data.repository

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.AnalysisResult
import com.casemapai.domain.model.AnalysisType
import com.casemapai.domain.prompt.IssueSpotterPromptBuilder
import com.casemapai.domain.repository.AnalysisRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class AnalysisRepositoryImpl @Inject constructor(
    private val aiClient: AiClient,
    private val issueSpotterPromptBuilder: IssueSpotterPromptBuilder
) : AnalysisRepository {

    private val results = MutableStateFlow<List<AnalysisResult>>(emptyList())

    override fun observeResults(matterId: UUID): Flow<List<AnalysisResult>> {
        return results.map { allResults ->
            allResults.filter { it.matterId == matterId }
        }
    }

    override suspend fun analyzeText(
        matter: GetMatterQuery.Data.Matter,
        analysisType: AnalysisType
    ): AnalysisResult {

        // 1️⃣ Build prompt
        val prompt = when (analysisType) {
            AnalysisType.ISSUE_SPOTTER -> issueSpotterPromptBuilder.build(matter)
            // future:
            // AnalysisType.ASYLUM -> asylumPromptBuilder.build(matter)
            else -> issueSpotterPromptBuilder.build(matter)
        }

        // 2️⃣ Call Gemini
        val response = aiClient.analyze(prompt)

        // 3️⃣ Create metadata
        val nextId = (results.value.maxByOrNull { it.id }?.id ?: 0L) + 1L
        val now = System.currentTimeMillis()

        // 4️⃣ Map DTO → Domain
        val result = AnalysisResult(
            id = (results.value.maxByOrNull { it.id }?.id ?: 0L) + 1L,
            matterId = matter.id,
            analysisType = analysisType,
            body = response.body,
            provider = response.provider,
            modelName = response.modelName,
            createdAt = System.currentTimeMillis()
        )

        // 5️⃣ Store in memory (for now)
        results.value = listOf(result) + results.value

        return result
    }
}