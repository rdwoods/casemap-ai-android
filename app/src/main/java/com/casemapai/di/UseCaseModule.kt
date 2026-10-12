package com.casemapai.di

import com.casemapai.data.repository.AnalysisRepositoryImpl
import com.casemapai.domain.repository.AnalysisRepository
import com.casemapai.domain.repository.MatterRepository
import com.casemapai.domain.usecase.GetMatterUseCase
import com.casemapai.domain.usecase.GetMattersUseCase
import com.casemapai.domain.usecase.RunIssueSpotterUseCase
import com.casemapai.domain.usecase.SaveMatterUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetMattersUseCase(
        repository: MatterRepository
    ): GetMattersUseCase = GetMattersUseCase(repository)

    @Provides
    fun provideGetMatterUseCase(
        repository: MatterRepository
    ): GetMatterUseCase = GetMatterUseCase(repository)

    @Provides
    fun provideSaveMatterUseCase(
        repository: MatterRepository
    ): SaveMatterUseCase = SaveMatterUseCase(repository)

    @Provides
    fun provideRunIssueSpotterUseCase(
        repository: AnalysisRepository
    ): RunIssueSpotterUseCase = RunIssueSpotterUseCase(repository)
}

/*
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    */
/*@Binds
    @Singleton
    abstract fun bindMatterRepository(
        impl: MatterRepositoryImpl
    ): MatterRepository
*//*

    @Binds
    @Singleton
    abstract fun bindAnalysisRepository(
        impl: AnalysisRepositoryImpl
    ): AnalysisRepository
}*/
