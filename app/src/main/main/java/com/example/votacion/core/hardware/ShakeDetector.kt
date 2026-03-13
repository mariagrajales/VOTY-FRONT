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

    private val shakeThreshold = 8.0f  // umbral razonable para aceleración lineal sin gravedad
    private var lastShakeTime = 0L
    private val debounceTime = 1000  // 1 segundo entre agitaciones
    private var gravity = FloatArray(3)

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Filtro paso alto para eliminar la gravedad
                val alpha = 0.8f
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                val linearX = event.values[0] - gravity[0]
                val linearY = event.values[1] - gravity[1]
                val linearZ = event.values[2] - gravity[2]

                val acceleration = sqrt((linearX * linearX + linearY * linearY + linearZ * linearZ).toDouble())
                val currentTime = System.currentTimeMillis()

                if (acceleration > shakeThreshold &&
                    currentTime - lastShakeTime > debounceTime
                ) {
                    android.util.Log.d("ShakeDetector", "Shake detectado! Fuerza: \$acceleration")
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
