package com.example.votacion.features.auth.domain.usecase

import com.example.votacion.features.auth.domain.repository.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, name: String, password: String): Result<Unit> {
        return try {
            repository.register(email, name, password)
            Result.success(Unit)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            val message = when {
                errorBody.contains("already registered") -> "Este correo ya está registrado"
                e.code() == 400 -> "Datos inválidos"
                else -> "Error al crear cuenta"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión"))
        }
    }
}