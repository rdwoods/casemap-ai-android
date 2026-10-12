package com.casemapai.data.repository

interface AiClient {
    suspend fun analyze(prompt: String): AiResponse
}