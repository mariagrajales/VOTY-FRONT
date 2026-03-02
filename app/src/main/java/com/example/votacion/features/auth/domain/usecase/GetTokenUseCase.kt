package com.example.votacion.features.auth.domain.usecase

import com.example.votacion.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<String> {
        return repository.getTokenFlow()
    }
}
