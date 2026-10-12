package com.casemapai.data.service

import com.casemapai.data.remote.dto.AnalyzeRequestDto
import com.casemapai.data.remote.dto.AnalyzeResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApiService {

    @POST("analyzeWithGemini")
    suspend fun generateContent(
        @Body request: AnalyzeRequestDto
    ): AnalyzeResponseDto
}
