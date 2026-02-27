package com.example.votacion.core.di

import com.example.votacion.features.auth.data.network.AuthService
import com.example.votacion.features.polls.data.network.PollService
import com.example.votacion.features.auth.data.repository.AuthRepository
import com.example.votacion.features.polls.data.repository.PollRepository
import com.example.votacion.core.data.AuthPreferences
import com.example.votacion.core.data.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideAuthRepository(
        authService: AuthService,
        authPreferences: AuthPreferences,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(authService, authPreferences, tokenManager)
    }

    @Singleton
    @Provides
    fun providePollRepository(
        pollService: PollService
    ): PollRepository {
        return PollRepository(pollService)
    }
}
