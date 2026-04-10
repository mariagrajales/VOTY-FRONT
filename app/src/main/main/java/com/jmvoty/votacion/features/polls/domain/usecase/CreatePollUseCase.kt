package com.jmvoty.votacion.features.polls.domain.usecase

import com.jmvoty.votacion.features.polls.domain.repository.PollRepository
import com.jmvoty.votacion.features.polls.presentation.viewmodel.OptionDraft
import javax.inject.Inject

class CreatePollUseCase @Inject constructor(
    private val repository: PollRepository
) {
    suspend operator fun invoke(title: String, options: List<OptionDraft>): Result<Unit> {
        return try {
            // Validación de negocio: No permitir opciones vacías
            val validOptions = options.filter { it.text.isNotBlank() }
            if (validOptions.size < 2) {
                return Result.failure(Exception("Se requieren al menos 2 opciones válidas"))
            }

            repository.createPoll(title, validOptions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}