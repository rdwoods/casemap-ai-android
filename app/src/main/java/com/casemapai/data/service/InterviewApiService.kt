package com.casemapai.data.service

import com.casemapai.data.remote.dto.CallableRequest
import com.casemapai.data.remote.dto.CallableResponse
import com.casemapai.data.remote.dto.InterviewTurnRequestDto
import com.casemapai.data.remote.dto.InterviewTurnResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface InterviewApiService {

    @POST("guidedInterviewTurn")
    suspend fun nextTurn(
        @Body request: CallableRequest<InterviewTurnRequestDto>
    ): CallableResponse<InterviewTurnResponseDto>
}
