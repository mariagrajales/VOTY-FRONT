package com.jmvoty.votacion.core.di

import com.jmvoty.votacion.features.auth.data.repository.AuthRepositoryImpl
import com.jmvoty.votacion.features.auth.domain.repository.AuthRepository
import com.jmvoty.votacion.features.polls.data.repository.PollRepositoryImpl
import com.jmvoty.votacion.features.polls.domain.repository.PollRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Singleton
    @Binds
    abstract fun bindPollRepository(
        pollRepositoryImpl: PollRepositoryImpl
    ): PollRepository

    @Singleton
    @Binds
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}