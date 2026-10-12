package com.casemapai.domain.repository

import com.casemapai.data.remote.dto.GeminiGenerateContentResponseDto
import com.casemapai.data.remote.dto.GeminiResponseDto
import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter
import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface MatterRepository {
    suspend fun getMatters(): List<GetMatterQuery.Data.Matter>
    fun observeMatters(): Flow<List<GetMatterQuery.Data.Matter>>
    suspend fun getMatter(matterId: UUID): GetMatterQuery.Data.Matter?
    suspend fun saveMatter(matter: GetMatterQuery.Data.Matter): UUID
}
