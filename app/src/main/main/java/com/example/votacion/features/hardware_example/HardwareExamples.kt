package com.example.votacion.features.hardware_example

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.votacion.core.hardware.ShakeDetector
import com.example.votacion.core.hardware.VibratorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- SETTINGS MOCK ---

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val vibratorManager: VibratorManager
) : ViewModel() {

    private val _isVibrationEnabled = MutableStateFlow(false)
    val isVibrationEnabled: StateFlow<Boolean> = _isVibrationEnabled.asStateFlow()

    init {
        loadVibrationSetting()
    }

    private fun loadVibrationSetting() {
        viewModelScope.launch {
            _isVibrationEnabled.value = vibratorManager.isVibrationEnabled()
        }
    }

    fun onVibrationEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            vibratorManager.setVibrationEnabled(enabled)
            _isVibrationEnabled.value = enabled

            // Feedback visual (vibrar para probar)
            if (enabled) {
                vibratorManager.vibrate(50)
            }
        }
    }
}

@Composable
fun SettingsScreenExample(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isVibrationEnabled by viewModel.isVibrationEnabled.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración de Hardware",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.onVibrationEnabledChanged(!isVibrationEnabled) }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Vibración Global",
                style = MaterialTheme.typography.bodyLarge
            )
            Switch(
                checked = isVibrationEnabled,
                onCheckedChange = { viewModel.onVibrationEnabledChanged(it) }
            )
        }

        Text(
            text = "Al desactivar la vibración, la app no vibrará en ninguna interacción en la app globalmente",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// --- SURVEYS MOCK ---

data class Survey(val id: String, val title: String)

data class SurveysUIState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val surveys: List<Survey> = emptyList()
)

class RefreshSurveysUseCase @Inject constructor() {
    operator fun invoke(): Flow<List<Survey>> = flow {
        delay(1500) // Simulate network call
        emit(
            listOf(
                Survey("1", "Encuesta de Satisfacción ${System.currentTimeMillis()}"),
                Survey("2", "Evaluación de Clima Laboral"),
                Survey("3", "Feedback de Producto")
            )
        )
    }
}

@HiltViewModel
class SurveysViewModel @Inject constructor(
    val shakeDetector: ShakeDetector,
    private val refreshSurveysUseCase: RefreshSurveysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SurveysUIState())
    val uiState: StateFlow<SurveysUIState> = _uiState.asStateFlow()

    init {
        // Load initial surveys
        loadSurveys()
        observeShakeEvents()
    }

    private fun loadSurveys() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            refreshSurveysUseCase().collect { result ->
                _uiState.update { state -> state.copy(surveys = result, isLoading = false) }
            }
        }
    }

    private fun observeShakeEvents() {
        viewModelScope.launch {
            shakeDetector.shakeEvents.collect {
                // Activar animación de recarga
                _uiState.update { it.copy(isRefreshing = true) }

                // Ejecutar recarga de encuestas
                refreshSurveysUseCase().collect { result ->
                    _uiState.update { state ->
                        state.copy(
                            surveys = result,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector.stopListening()
    }
}

@Composable
fun SurveysScreenExample(
    viewModel: SurveysViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Activar detector cuando la pantalla está visible
    DisposableEffect(Unit) {
        viewModel.shakeDetector.startListening()
        onDispose {
            viewModel.shakeDetector.stopListening()
        }
    }

    // UI con animación de recarga
    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                items(uiState.surveys) { survey ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = survey.title,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        if (uiState.isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }
}
