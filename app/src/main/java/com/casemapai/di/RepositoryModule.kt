package com.casemapai.di

import com.casemapai.data.repository.AiClient
import com.casemapai.data.repository.AnalysisRepositoryImpl
import com.casemapai.domain.prompt.IssueSpotterPromptBuilder
import com.casemapai.domain.repository.AnalysisRepository
import com.casemapai.domain.repository.MatterRepository
import com.casemapai.domain.repository.MatterRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideIssueSpotterPromptBuilder(): IssueSpotterPromptBuilder {
        return IssueSpotterPromptBuilder()
    }

    @Provides
    @Singleton
    fun provideAnalysisRepository(
        aiClient: AiClient,
        promptBuilder: IssueSpotterPromptBuilder
    ): AnalysisRepository {
        return AnalysisRepositoryImpl(
            aiClient,
            promptBuilder
        )
    }

    @Provides
    @Singleton
    fun provideMatterRepository(
        sqlConnectDataSource: SqlConnectDataSource
    ): MatterRepository {
        return MatterRepositoryImpl(sqlConnectDataSource = sqlConnectDataSource)
    }
}