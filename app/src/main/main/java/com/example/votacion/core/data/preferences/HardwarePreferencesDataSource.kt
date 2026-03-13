package com.example.votacion.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Preferencias globales (NO por usuario)
object HardwarePreferencesKeys {
    const val VIBRATION_ENABLED = "vibration_enabled"
}

// Extensión para simplificar el acceso, creándolo a nivel de Context
private val Context.hardwareDataStore by preferencesDataStore(name = "hardware_preferences")


// DataSource
interface HardwarePreferencesDataSource {
    suspend fun isVibrationEnabled(): Boolean
    suspend fun setVibrationEnabled(enabled: Boolean)
}

// Implementación con DataStore
class HardwarePreferencesDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HardwarePreferencesDataSource {
    
    override suspend fun isVibrationEnabled(): Boolean =
        context.hardwareDataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(HardwarePreferencesKeys.VIBRATION_ENABLED)] ?: true
        }.first()
    
    override suspend fun setVibrationEnabled(enabled: Boolean) {
        context.hardwareDataStore.edit { preferences ->
            preferences[booleanPreferencesKey(HardwarePreferencesKeys.VIBRATION_ENABLED)] = enabled
        }
    }
}
