# Aplicación de Votación - Documentación

## Descripción General
Aplicación Android nativa usando Jetpack Compose y Material 3 que implementa un sistema de encuestas/votaciones robusto con:
- Autenticación (Login/Registro)
- Creación de encuestas
- Visualización de encuestas en cards
- Sistema de votación
- Gestión segura de tokens JWT

## Arquitectura
Se utilizó la arquitectura MVVM con Hilt para inyección de dependencias:

```
data/
├── models/              # Modelos de datos (Parcelable/Serializable)
│   ├── AuthModels.kt
│   └── PollModels.kt
├── network/             # Servicios de API (Retrofit)
│   ├── AuthService.kt
│   └── PollService.kt
├── repository/          # Repositorios (lógica de acceso a datos)
│   ├── AuthRepository.kt
│   └── PollRepository.kt

core/
├── data/                # Preferencias (DataStore)
│   └── AuthPreferences.kt
├── di/                  # Módulos de inyección de dependencias
│   ├── NetworkModule.kt
│   ├── DataStoreModule.kt
│   └── RepositoryModule.kt
├── network/             # Interceptores
│   └── AuthInterceptor.kt

presentation/
├── viewmodel/           # ViewModels con MutableStateOf
│   ├── AuthViewModel.kt
│   ├── PollsViewModel.kt
│   └── CreatePollViewModel.kt
├── screens/             # Composables de pantallas
│   ├── LoginScreen.kt
│   ├── RegisterScreen.kt
│   ├── PollsScreen.kt
│   └── CreatePollScreen.kt

navigation/
├── Screen.kt
└── NavigationGraph.kt

VotacionApp.kt          # Application con @HiltAndroidApp
MainActivity.kt         # Actividad principal
```

## Características Implementadas

### 1. Autenticación
- **LoginScreen**: Permite al usuario iniciar sesión
- **RegisterScreen**: Permite crear nueva cuenta
- Token JWT almacenado en DataStore
- Interceptor automático para agregar token a peticiones

### 2. Encuestas
- **PollsScreen**: Lista todas las encuestas en cards
- **CreatePollScreen**: Crear nuevas encuestas con múltiples opciones
- Muestra contador de votos y porcentajes
- Indica si el usuario ya votó
- Muestra estado de la encuesta (abierta/cerrada)

### 3. UI/UX
- Material Design 3
- Tema dinámico (claro/oscuro)
- Navegación fluida
- Estados de carga
- Manejo de errores

## Dependencias Principales
- **Hilt**: Inyección de dependencias
- **Retrofit + Gson**: Cliente HTTP
- **Jetpack Compose**: Framework UI
- **Navigation Compose**: Navegación
- **DataStore**: Almacenamiento seguro de datos
- **Material 3**: Componentes de diseño

## Configuración de API

### URL Base
La URL base se configura en `build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL_UPRED", "\"http://10.0.2.2:8000/\"")  // Dev
buildConfigField("String", "BASE_URL_UPRED", "\"http://34.239.246.103:8000/\"")  // Prod
```

### Endpoints Utilizados
- `POST /auth/register` - Registro
- `POST /auth/login` - Inicio de sesión
- `GET /auth/profile` - Perfil de usuario
- `GET /polls` - Listar encuestas
- `POST /polls` - Crear encuesta
- `GET /polls/{id}` - Obtener encuesta
- `POST /polls/{poll_id}/vote/{option_id}` - Votar

## Estados y Flujo de Datos

### AuthViewModel
```
Estado: AuthUiState
- isLoading: Boolean
- email: String
- password: String
- name: String
- error: String?
- isAuthenticated: Boolean
- userName: String
```

### PollsViewModel
```
Estado: PollsUiState
- polls: List<PollOutput>
- isLoading: Boolean
- error: String?
```

### CreatePollViewModel
```
Estado: CreatePollUiState
- title: String
- options: List<String>
- isLoading: Boolean
- error: String?
- success: Boolean
```

## Seguridad
- Token JWT almacenado en DataStore (encriptado por el sistema)
- Interceptor automático para incluir token en headers
- Validación de entrada en formularios
- Manejo de errores HTTP

## Compilación
### Requisitos
- Android Studio Koala o superior
- Kotlin 2.2.10
- Java 21
- SDK 36 (targetSdk)
- MinSdk 26

### Pasos de Compilación
1. Sincronizar Gradle
2. Build → Make Project
3. Run → Run 'app'

## Notas de Desarrollo
- El localhost en emulador es `10.0.2.2`
- DataStore requiere permisos en AndroidManifest.xml
- Hilt requiere la anotación @HiltAndroidApp en Application
- Los ViewModels usan MutableStateOf para reactividad
- LaunchedEffect se usa para efectos secundarios en composables

## Próximas Mejoras
- WebSocket para actualizaciones en tiempo real
- Paginación de encuestas
- Filtros y búsqueda
- Estadísticas detalladas
- Internacionalización (i18n)
- Temas personalizados
