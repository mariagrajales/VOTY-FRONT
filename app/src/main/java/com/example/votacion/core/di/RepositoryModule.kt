package com.example.votacion.core.di

import com.example.votacion.features.auth.data.network.AuthService
import com.example.votacion.features.polls.data.network.PollService
import com.example.votacion.features.auth.data.repository.AuthRepository
import com.example.votacion.features.polls.data.repository.PollRepositoryImpl
import com.example.votacion.features.polls.domain.repository.PollRepository
import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.core.data.TokenManager
import dagger.Binds
import dagger.Module
import dagger.Provides
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

    companion object {
        @Singleton
        @Provides
        fun provideAuthRepository(
            authService: AuthService,
            authPreferences: AuthPreferences,
            tokenManager: TokenManager
        ): AuthRepository {
            return AuthRepository(authService, authPreferences, tokenManager)
        }
    }
}
