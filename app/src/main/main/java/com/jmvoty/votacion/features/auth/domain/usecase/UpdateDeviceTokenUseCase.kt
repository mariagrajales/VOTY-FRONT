package com.jmvoty.votacion.features.auth.domain.usecase

import com.jmvoty.votacion.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class UpdateDeviceTokenUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(token: String) {
        repository.updateDeviceToken(token)
    }
}