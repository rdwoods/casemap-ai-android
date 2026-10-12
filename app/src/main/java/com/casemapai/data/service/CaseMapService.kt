package com.casemapai.data.service

import com.casemapai.data.response.OpenAiRequestDto
import com.casemapai.data.response.OpenAiResponseDto
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


/**
 * Created by Rahman Woods on 3/28/2026.
 */

interface CaseMapService {
    @POST("v1/responses")
    suspend fun createResponse(
        @Header("Authorization") authorization: String,
        @Body request: OpenAiRequestDto
    ): OpenAiResponseDto
}