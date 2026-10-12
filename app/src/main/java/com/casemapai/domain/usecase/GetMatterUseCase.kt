package com.casemapai.domain.usecase

import com.casemapai.domain.repository.MatterRepository
import java.util.UUID

class GetMatterUseCase(
    private val repository: MatterRepository
) {
    suspend operator fun invoke(matterId: UUID) = repository.getMatter(matterId)
}
