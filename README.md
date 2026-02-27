# Aplicación de Votación Android

Una aplicación Android moderna para crear y gestionar encuestas/votaciones con autenticación JWT, construida con Jetpack Compose y siguiendo la arquitectura MVVM.

## Requisitos Previos

- androidStudioProjects/Votacion$ instalado
- API GO de votación funcionando (ver API_VOTY)
- Android SDK 36 (asegúrate de tener `platform-tools`/`adb` en tu PATH para ver logs)
- Kotlin 2.2.10

> ⚠️ **Importante**: los sabores (`dev`/`prod`) sobrescriben el nombre de la aplicación. Se ha actualizado para que el nombre sea "Votacion (DEV)" en modo dev y "Votacion" en producción. Si ves otras etiquetas como "Demo" o "Voty", desinstala las versiones anteriores antes de reinstalar.


## Inicio Rápido

### 1. Configurar la API
La API ya está desplegada en `https://apivoty.jhonatanzc.fun/ETC`, no es necesario correrla localmente.

### 2. Configurar la URL de la API
El proyecto ya apunta a la URL desplegada en `build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL_UPRED", "\"https://apivoty.jhonatanzc.fun/ETC\"")
```

### 3. Compilar y ejecutar
```bash
# Sincronizar Gradle
./gradlew sync

# Compilar
./gradlew build

# Ejecutar en emulador o dispositivo
./gradlew installDebug
```

## Pantallas Disponibles

### 🔐 Login
- Inicia sesión con email y contraseña
- Navega a registro para nuevos usuarios
- Valida credenciales con la API

### 📝 Registro
- Crea nueva cuenta con email, nombre y contraseña
- Validaciones de formulario
- Almacena token automáticamente

### 🗳️ Encuestas (Pantalla Principal)
- Lista todas las encuestas disponibles
- Muestra información completa de cada encuesta:
  - Título
  - Opciones de voto
  - Contador de votos por opción
  - Porcentaje de votos
  - Indicador de si ya votaste
- Botón flotante para crear nueva encuesta
- Botón de cerrar sesión

### ➕ Crear Encuesta
- Campo para ingresar el título
- Agregar/remover opciones (mínimo 2)
- Validación de inputs
- Confirmación de creación

## Flujo de Autenticación

1. Usuario abre la app
2. Si no tiene token → Pantalla de Login
3. Login exitoso → Almacena token y navega a Encuestas
4. Token se incluye automáticamente en requests
5. Botón Logout → Borra token y regresa a Login

## Características Destacadas

✨ **Material Design 3** - UI moderna y consistente
🔒 **Autenticación JWT** - Seguridad con tokens
💾 **DataStore** - Almacenamiento seguro de datos
🎯 **MVVM + Hilt** - Arquitectura limpia
📱 **Jetpack Compose** - UI reactiva
🔄 **Estado reactivo** - MutableStateOf para UI updates

## Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/votacion/
│   │   ├── core/                    # DI, interceptores, datastore, utilidades
│   │   ├── features/                # Código organizado por característica
│   │   │   ├── auth/                # Login/registro
│   │   │   │   ├── data/            # Modelos, servicio, repositorio
│   │   │   │   └── presentation/    # ViewModels y pantallas
│   │   │   └── polls/               # Encuestas
│   │   │       ├── data/            # Modelos, servicio, repositorio
│   │   │       └── presentation/    # ViewModels y pantallas
│   │   ├── navigation/              # Navegación
│   │   ├── VotacionApp.kt           (Application)
│   │   └── MainActivity.kt          (Actividad principal)
│   ├── AndroidManifest.xml
│   └── res/                         (Recursos)
└── build.gradle.kts
```

## Configuración de Hilt

Hilt está totalmente configurado con:
- `@HiltAndroidApp` en VotacionApp
- `@AndroidEntryPoint` en MainActivity
- `@HiltViewModel` en ViewModels
- Módulos DI en `core/di/`

## Endpoints de API Utilizados

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/register` | Registrar usuario |
| POST | `/auth/login` | Iniciar sesión |
| GET | `/auth/profile` | Obtener perfil |
| GET | `/polls` | Listar encuestas |
| POST | `/polls` | Crear encuesta |
| POST | `/polls/{id}/vote/{option_id}` | Votar |

## Troubleshooting

### Error de conexión a API
- Verifica que la API esté corriendo
- Revisa la URL en build.gradle.kts
- En emulador, usa `10.0.2.2` para localhost

### Errores de compilación
- Sincronizar Gradle: `./gradlew sync`
- Limpiar build: `./gradlew clean`
- Invalidar caches: File → Invalidate Caches

### Token no persiste
- Verifica que AuthPreferences esté inyectado
- Comprueba permisos en AndroidManifest.xml

## Desarrollo

### Agregar nueva feature
1. Crear modelo en `data/models/`
2. Crear servicio API en `data/network/`
3. Crear repositorio en `data/repository/`
4. Crear ViewModel en `presentation/viewmodel/`
5. Crear screen en `presentation/screens/`
6. Agregar ruta en `navigation/Screen.kt`
7. Agregar composable en `NavigationGraph.kt`

### Testing
```bash
# Tests unitarios
./gradlew test

# Tests de UI
./gradlew connectedAndroidTest
```

## Licencia
Proyecto de ejemplo - Libre para usar y modificar

## Soporte
Para problemas o sugerencias, contacta al desarrollador.

---

**Última actualización:** Febrero 2026
**Versión:** 1.0
