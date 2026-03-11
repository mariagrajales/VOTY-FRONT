package com.example.votacion.features.profile.presentation.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
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
                Log.d("ProfileViewModel", "Starting avatar update. Base64 length: ${base64Image.length}")
                _uiState.update { it.copy(isUpdating = true) }

                val result = updateProfileUseCase(avatar = base64Image)

                _uiState.update { currentState ->
                    result.fold(
                        onSuccess = { user ->
                            Log.d("ProfileViewModel", "Avatar update SUCCESS")
                            vibrationManager.vibrateSuccess()
                            currentState.copy(
                                user = user,
                                isUpdating = false,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            Log.e("ProfileViewModel", "Avatar update FAILED: ${error.message}")
                            vibrationManager.vibrateError()
                            currentState.copy(
                                isUpdating = false,
                                error = error.message
                            )
                        }
                    )
                }

            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Exception in updateAvatar", e)
                vibrationManager.vibrateError()
                _uiState.update { it.copy(isUpdating = false, error = e.message) }
            }
        }
    }

    fun updateAvatar(uri: Uri) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating avatar from URI: $uri")
            val base64 = uriToBase64(uri)
            if (base64 != null) {
                executeAvatarUpdate(base64)
            } else {
                Log.e("ProfileViewModel", "Failed to convert URI to Base64")
            }
        }
    }

    fun updateAvatar(bitmap: Bitmap) {
        viewModelScope.launch {
            Log.d("ProfileViewModel", "Updating avatar from Bitmap")
            val base64 = bitmapToBase64(bitmap)
            executeAvatarUpdate(base64)
        }
    }

    private suspend fun executeAvatarUpdate(base64: String) {
        Log.d("ProfileViewModel", "Executing avatar update. Base64 preview: ${base64.take(30)}...")
        _uiState.update { it.copy(isUpdating = true) }
        val result = updateProfileUseCase(avatar = base64)
        _uiState.update { state ->
            result.fold(
                onSuccess = { user ->
                    Log.d("ProfileViewModel", "Execution SUCCESS")
                    vibrationManager.vibrateSuccess()
                    state.copy(user = user, isUpdating = false, error = null)
                },
                onFailure = { e ->
                    Log.e("ProfileViewModel", "Execution FAILED: ${e.message}")
                    vibrationManager.vibrateError()
                    state.copy(isUpdating = false, error = e.message)
                }
            )
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                val scaled = Bitmap.createScaledBitmap(bitmap, 500, 500, true)
                bitmapToBase64(scaled)
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error in uriToBase64", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}
