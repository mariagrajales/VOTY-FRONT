package com.example.votacion.core.hardware

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager as AndroidVibratorManager
import com.example.votacion.core.data.preferences.HardwarePreferencesDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

interface VibratorManager {
    suspend fun isVibrationEnabled(): Boolean
    suspend fun setVibrationEnabled(enabled: Boolean)
    fun vibrate(duration: Long = 300)
    fun vibrate(pattern: LongArray, repeat: Int = -1)
}

class VibratorManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesDataSource: HardwarePreferencesDataSource
) : VibratorManager {

    // Helper scope for launching vibration checks
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as AndroidVibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override suspend fun isVibrationEnabled(): Boolean =
        preferencesDataSource.isVibrationEnabled()

    override suspend fun setVibrationEnabled(enabled: Boolean) {
        preferencesDataSource.setVibrationEnabled(enabled)
    }

    override fun vibrate(duration: Long) {
        scope.launch {
            if (isVibrationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        }
    }

    override fun vibrate(pattern: LongArray, repeat: Int) {
        scope.launch {
            if (isVibrationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, repeat)
                }
            }
        }
    }
}
