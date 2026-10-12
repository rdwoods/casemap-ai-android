package com.casemapai.domain.usecase

import com.casemapai.dataconnect.GetMatterQuery
import com.casemapai.domain.model.Matter
import com.casemapai.domain.repository.MatterRepository
import java.util.UUID

class SaveMatterUseCase(
    private val repository: MatterRepository
) {
    suspend operator fun invoke(matter: GetMatterQuery.Data.Matter): UUID = repository.saveMatter(matter)
}
