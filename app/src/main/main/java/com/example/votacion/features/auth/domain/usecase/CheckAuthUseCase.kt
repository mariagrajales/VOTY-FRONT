package com.example.votacion.features.auth.domain.usecase

import com.example.votacion.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    // Devuelve un Flow de Boolean para saber en tiempo real si hay sesión
    operator fun invoke(): Flow<Boolean> {
        return repository.getTokenFlow().map { token ->
            token.isNotEmpty()
        }
    }
}