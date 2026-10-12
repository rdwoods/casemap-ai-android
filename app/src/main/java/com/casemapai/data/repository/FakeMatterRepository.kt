package com.casemapai.data.repository

import com.casemapai.domain.model.Matter
import com.casemapai.domain.repository.MatterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/*
class FakeMatterRepository : MatterRepository {
    private val matters = MutableStateFlow(
        listOf(
            Matter(
                id = 1L,
                title = "Parole case notes",
                summary = "Respondent released for a short period. Need generic issue review.",
                proceduralPosture = "Master calendar completed",
                disputedFacts = "Length and conditions of release are contested",
                notes = "Use sanitized facts only",
                updatedAtLabel = "Today"
            )
        )
    )

    override fun observeMatters(): Flow<List<Matter>> = matters

    override suspend fun getMatter(matterId: Long): Matter? {
        return matters.value.firstOrNull { it.id == matterId }
    }

    override suspend fun saveMatter(matter: Matter): Long {
        return if (matter.id == 0L) {
            val nextId = (matters.value.maxOfOrNull { it.id } ?: 0L) + 1L
            matters.value = listOf(matter.copy(id = nextId)) + matters.value
            nextId
        } else {
            matters.value = matters.value.map {
                if (it.id == matter.id) matter else it
            }
            matter.id
        }
    }
}
*/
