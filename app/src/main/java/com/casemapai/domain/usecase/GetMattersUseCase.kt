package com.casemapai.domain.usecase

import com.casemapai.domain.repository.MatterRepository

class GetMattersUseCase(
    private val repository: MatterRepository
) {
    operator fun invoke() = repository.observeMatters()
}
