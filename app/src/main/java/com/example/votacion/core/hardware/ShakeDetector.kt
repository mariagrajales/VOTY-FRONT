package com.example.votacion.core.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.sqrt

interface ShakeDetector {
    fun startListening()
    fun stopListening()
    val shakeEvents: Flow<ShakeEvent>
}

data class ShakeEvent(
    val timestamp: Long,
    val force: Float  // intensidad de la agitación
)

class ShakeDetectorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ShakeDetector {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _shakeEvents = MutableSharedFlow<ShakeEvent>()
    override val shakeEvents: Flow<ShakeEvent> = _shakeEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val shakeThreshold = 15.0  // ajustable
    private var lastShakeTime = 0L
    private val debounceTime = 1000  // 1 segundo entre agitaciones

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                val acceleration = sqrt((x * x + y * y + z * z).toDouble())
                val currentTime = System.currentTimeMillis()

                if (acceleration > shakeThreshold &&
                    currentTime - lastShakeTime > debounceTime
                ) {
                    lastShakeTime = currentTime
                    scope.launch {
                        _shakeEvents.emit(
                            ShakeEvent(
                                timestamp = currentTime,
                                force = acceleration.toFloat()
                            )
                        )
                    }
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(
                sensorListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun stopListening() {
        sensorManager.unregisterListener(sensorListener)
    }
}
