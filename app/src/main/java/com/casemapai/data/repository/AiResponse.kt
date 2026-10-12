package com.casemapai.data.repository

import com.casemapai.domain.model.AiProvider

data class AiResponse(
    val body: String,
    val provider: AiProvider,
    val modelName: String
)