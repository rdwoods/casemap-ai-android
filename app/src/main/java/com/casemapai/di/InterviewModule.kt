package com.casemapai.di

import com.casemapai.data.repository.FakeInterviewRepository
import com.casemapai.data.repository.InterviewRepositoryImpl
import com.casemapai.data.service.InterviewApiService
import com.casemapai.domain.repository.InterviewRepository
import com.casemapai.domain.usecase.NextInterviewStepUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InterviewModule {

    /** Flip to true to run the whole flow offline with scripted questions (no Gemini calls). */
    private const val USE_FAKE_AI = false

    @Provides
    @Singleton
    fun provideInterviewApiService(retrofit: Retrofit): InterviewApiService =
        retrofit.create(InterviewApiService::class.java)

    @Provides
    @Singleton
    fun provideInterviewRepository(
        real: Provider<InterviewRepositoryImpl>,
        fake: Provider<FakeInterviewRepository>
    ): InterviewRepository = if (USE_FAKE_AI) fake.get() else real.get()

    @Provides
    fun provideNextInterviewStepUseCase(
        repository: InterviewRepository
    ): NextInterviewStepUseCase = NextInterviewStepUseCase(repository)
}
