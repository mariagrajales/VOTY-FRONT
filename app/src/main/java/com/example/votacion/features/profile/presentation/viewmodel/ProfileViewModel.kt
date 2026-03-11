package com.example.votacion.features.profile.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.core.hardware.VibrationManager
import com.example.votacion.features.profile.data.models.ProfileResponse
import com.example.votacion.features.profile.domain.entities.UserProfile
import com.example.votacion.features.profile.domain.usecase.GetProfileUseCase
import com.example.votacion.features.profile.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.inject.Inject



@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val vibrationManager: VibrationManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = getProfileUseCase()
            _uiState.update { state ->
                result.fold(
                    onSuccess = { user -> state.copy(isLoading = false, user = user) },
                    onFailure = { error -> state.copy(isLoading = false, error = error.message) }
                )
            }
        }
    }


    fun updateAvatar(base64Image: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isUpdating = true) }

                val result = updateProfileUseCase(avatar = base64Image)

                _uiState.update { currentState ->
                    result.fold(
                        onSuccess = { user ->
                            currentState.copy(
                                user = user, // <-- AQUÍ usamos el 'user' retornado, no el use case
                                isUpdating = false,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            currentState.copy(
                                isUpdating = false,
                                error = error.message
                            )
                        }
                    )
                }

                // 3. Vibración solo si fue exitoso (opcional, según tu lógica)
                if (result.isSuccess) vibrationManager.vibrateSuccess()

            } catch (e: Exception) {
                vibrationManager.vibrateError()
                _uiState.update { it.copy(isUpdating = false, error = e.message) }
            }
        }
    }


    fun updateAvatar(uri: android.net.Uri) {
        viewModelScope.launch {
            val base64 = uriToBase64(uri) // Función auxiliar que ya deberías tener
            if (base64 != null) {
                executeAvatarUpdate(base64)
            }
        }
    }

    fun updateAvatar(bitmap: android.graphics.Bitmap) {
        viewModelScope.launch {
            val base64 = bitmapToBase64(bitmap) // Función auxiliar
            executeAvatarUpdate(base64)
        }
    }

    private suspend fun executeAvatarUpdate(base64: String) {
        _uiState.update { it.copy(isUpdating = true) }
        val result = updateProfileUseCase(avatar = base64)
        _uiState.update { state ->
            result.fold(
                onSuccess = { user -> state.copy(user = user, isUpdating = false) },
                onFailure = { error -> state.copy(isUpdating = false, error = error.message) }
            )
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bytes = inputStream.readBytes()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "Error converting URI to Base64", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}