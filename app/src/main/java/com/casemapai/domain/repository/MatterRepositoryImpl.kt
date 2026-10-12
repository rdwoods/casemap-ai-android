package com.casemapai.domain.repository

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.di.SqlConnectDataSource
import com.casemapai.domain.model.Matter
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class MatterRepositoryImpl @Inject constructor(
    val sqlConnectDataSource: SqlConnectDataSource
) : MatterRepository {

    override suspend fun getMatters(): List<GetMatterQuery.Data.Matter> {

        return emptyList() // stub for now
    }

    override fun observeMatters(): Flow<List<GetMatterQuery.Data.Matter>> {
        TODO("Not yet implemented")
    }

    override suspend fun getMatter(matterId: UUID): GetMatterQuery.Data.Matter? {
        TODO("Not yet implemented")
    }

    override suspend fun saveMatter(matter: GetMatterQuery.Data.Matter): UUID {
        return sqlConnectDataSource.createMatter(matter.id, matter.title, matter.summary, matter.proceduralPosture, matter.disputedFacts)

    }
}