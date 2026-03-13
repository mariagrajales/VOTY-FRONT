# Hardware Module

Este módulo centraliza el acceso a ciertas características de hardware del dispositivo móvil, como el Vibrador y el Acelerómetro, promoviendo una integración limpia en toda la aplicación bajo los principios de Clean Architecture.

## Componentes

### VibratorManager (`core/hardware/VibratorManager.kt`)
Provee acceso para ejecutar vibraciones con diferente duración o patrones específicos en el dispositivo.
Está ligado a una configuración global guardada en `HardwarePreferencesDataSource` respaldada por `DataStore`.

Si un usuario apaga la vibración globalmente desde la pantalla de configuración (Settings), todas las llamadas a `vibrate()` en cualquier parte de la aplicación serán ignoradas.

### ShakeDetector (`core/hardware/ShakeDetector.kt`)
Provee un observador a los eventos del Acelerómetro del dispositivo (`Sensor.TYPE_ACCELEROMETER`).
Puede detectar cuando el dispositivo es agitado fuertemente. Dispara eventos a través de un `Flow` al cual los `ViewModels` pueden suscribirse.

## Consideraciones de Rendimiento y Batería

- **Manejo del Ciclo de Vida del Sensor:**
  Es **crítico** llamar a `shakeDetector.startListening()` sólo cuando la pantalla correspondiente (ej. Pantalla de encuestas) sea visible para el usuario, y `shakeDetector.stopListening()` cuando es destruida o pasa a segundo plano.
  Dejar el sensor de aceleración escuchando permanentemente consumirá la batería del usuario muy rápidamente.
  Ejemplo en Compose usando `DisposableEffect`:
  ```kotlin
  DisposableEffect(Unit) {
      viewModel.shakeDetector.startListening()
      onDispose { viewModel.shakeDetector.stopListening() }
  }
  ```

- **Debounce de Eventos:**
  La implementación de `ShakeDetectorImpl` incluye un *debounce* predefinido de 1000 milisegundos (1 segundo). Esto evita que múltiples movimientos seguidos disparen funciones repetitivas en la UI de inmediato, mejorando el rendimiento general.

- **Clean Architecture & Inyección de Dependencias:**
  Los componentes interactúan utilizando interfaces. Toda la instanciación de dependencias manejando contexto Android está aislada en `HardwareModule`, implementado vía Hilt utilizando `@ApplicationContext`.
