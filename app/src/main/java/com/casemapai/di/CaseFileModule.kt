package com.casemapai.di

import com.casemapai.data.repository.InMemoryCaseFileRepository
import com.casemapai.domain.repository.CaseFileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CaseFileModule {

    /** Swap to DataConnectCaseFileRepository once the Data Connect SDK is generated. */
    @Binds
    @Singleton
    abstract fun bindCaseFileRepository(impl: InMemoryCaseFileRepository): CaseFileRepository
}
