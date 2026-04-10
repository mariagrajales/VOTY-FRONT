🗳️ VOTY - Sistema de Votación Inteligente (Android)
VOTY es una aplicación móvil de votación robusta, diseñada bajo principios de Clean Architecture y Offline-First. Permite a los usuarios participar en procesos democráticos digitales de manera fluida, incluso sin conexión a internet, aprovechando el hardware del dispositivo para una experiencia enriquecida.
🚀 Novedades y Mejoras (Corte 03)
Para esta entrega final, se han implementado las siguientes capacidades avanzadas:
•
Resiliencia Offline: Uso de Room y WorkManager para permitir votaciones y creación de encuestas sin internet, con sincronización automática posterior.
•
Interacción con Hardware:
◦
Acelerómetro: Implementación de Shake-to-Refresh para actualizar encuestas agitando el móvil.
◦
Feedback Háptico: Vibración confirmatoria al registrar un voto exitosamente.
•
Comunicación Proactiva: Integración de Firebase Cloud Messaging (FCM) para notificaciones push en tiempo real.
•
Arquitectura Evolucionada: Migración total a KSP para Hilt y Room, y estructura de carpetas por capas de dominio.

🛠️ Stack Tecnológico
•
UI: Jetpack Compose con Material Theme 3.0 (Soporte para colores dinámicos).
•
Arquitectura: MVVM + Clean Architecture (Data, Domain, Presentation).
•
Persistencia: Room Database (SSOT - Single Source of Truth).
•
Sincronización: WorkManager con restricciones de red.
•
Red: Retrofit 2.11 + Corrutinas de Kotlin.
•
DI: Hilt (Dagger) con navegación integrada.
•
Hardware: Sensor Manager (Acelerómetro) y Vibrator Service.
•
Mensajería: Firebase Cloud Messaging.
📱 Pantallas y Funcionalidades
🔐 Autenticación (MVP 1)
•
Login/Registro: Validación de JWT y persistencia de sesión segura mediante DataStore.
•
Seguridad: Interceptores de red para adjuntar el token automáticamente.
🗳️ Gestión de Encuestas (MVP 2)
•
Dashboard Dinámico: Visualización de resultados en tiempo real con porcentajes y contadores.
•
Creación Avanzada: Formulario dinámico para añadir múltiples opciones con validación en tiempo real.
🔄 Sincronización y Hardware (MVP 3)
•
Modo Offline: Los votos se guardan localmente en Room y un Worker se encarga de subirlos al recuperar conexión.
•
Shake-to-Refresh: Refresco visual activado por el movimiento del dispositivo.
•
Notificaciones: Alertas de nuevas encuestas disponibles.
🏗️ Estructura del Proyecto
app/src/main/main/java/com/jmvoty/votacion/
├── core/                    
│   ├── database/            # Configuración de Room
│   ├── di/                  # Módulos de Hilt (Network, Database, etc.)
│   ├── hardware/            # ShakeDetector, VibrationManager
│   └── notifications/       # VotacionMessagingService (FCM)
├── features/                # Organizado por Feature -> Capas
│   ├── auth/                
│   │   ├── data/ | domain/ | presentation/
│   ├── polls/               
│   │   ├── data/            # Repositorios y Workers (SyncVotesWorker)
│   │   ├── domain/          # Casos de Uso (UseCases)
│   │   └── presentation/    # ViewModels (StateFlow) y Screens
│   └── profile/
└── navigation/              # Grafo de navegación de Compose

⚙️ Configuración y Ejecución
1. Sabores (Flavors)
El proyecto utiliza dos entornos configurados en build.gradle.kts:
•
dev: Apunta a la API de desarrollo y añade "(DEV)" al nombre de la app.
•
prod: Configuración optimizada para producción.
2. Variables de Entorno
La URL base está centralizada en los flavors:
Kotlin
buildConfigField("String", "BASE_URL_UPRED", "\"https://apivoty.jhonatanzc.fun/\"")
3. Instalación
1.
Asegúrate de tener el archivo google-services.json en la carpeta /app.
2.
Sincroniza Gradle (Usa Java 21).
3.
Ejecuta la variante deseada desde la pestaña "Build Variants".

📊 Estrategia de Datos (Room + WorkManager)
La aplicación implementa un flujo de Single Source of Truth:
1.
El usuario realiza una acción (votar/crear).
2.
Los datos se guardan inmediatamente en Room.
3.
Se encola un OneTimeWorkRequest en WorkManager con la restricción NetworkType.CONNECTED.
4.
La UI observa un Flow de Room, por lo que el cambio es instantáneo para el usuario (optimistic UI).
📝 Conclusión
Este proyecto demuestra la capacidad de construir una aplicación empresarial que no depende de una conexión constante, utiliza los sensores del dispositivo para mejorar la accesibilidad y mantiene un código limpio y escalable siguiendo los estándares más exigentes de la industria Android actual.
