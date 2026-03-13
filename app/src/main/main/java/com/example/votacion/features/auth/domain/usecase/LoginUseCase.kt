package com.example.votacion.features.auth.domain.usecase

import com.example.votacion.features.auth.domain.repository.AuthRepository
import retrofit2.HttpException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return try {
            repository.login(email, password)
            Result.success(Unit)
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Correo o contraseña incorrectos"
                404 -> "El usuario no existe"
                else -> "Error del servidor (${e.code()})"
            }
            Result.failure(Exception(message))
        } catch (e: Exception) {
            Result.failure(Exception("Sin conexión al servidor"))
        }
    }
}