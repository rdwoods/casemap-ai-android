package com.casemapai.di

import com.casemapai.data.repository.AiClient
import com.casemapai.data.repository.AnalysisRepositoryImpl
import com.casemapai.dataconnect.GetUserQuery
import com.casemapai.domain.prompt.IssueSpotterPromptBuilder
import com.casemapai.domain.repository.AnalysisRepository
import com.casemapai.domain.repository.MatterRepository
import com.casemapai.domain.usecase.GetMattersUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUser(): GetUserQuery.Data.User {
        return GetUserQuery.Data.User(
            id = UUID.fromString("cf5a12e8-fa6d-4ac9-abfc-bb5ae8c02901"),
            email = "rdwoods1@gmail.com",
            displayName = "rdwoods"
        )
    }
}