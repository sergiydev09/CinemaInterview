# Cinema Interview App

A modern Android application built to showcase best practices for technical interviews. This app demonstrates trending movies and people using the TMDB API.

## Architecture

This project follows **Clean Architecture** principles with a modular approach:

### Module Structure

```
CinemaInterview/
├── app/                    # Application module
├── core/
│   ├── data/              # Networking, session, security, image URL utilities
│   ├── domain/            # Result wrapper, Flow extensions, SessionManager
│   └── ui/                # Base classes, UI components, extensions
└── feature/
    ├── login/
    │   ├── data/          # Repository implementations, data sources
    │   ├── domain/        # Use cases, repository interfaces, models
    │   └── ui/            # Fragments, ViewModels, adapters
    ├── home/
    │   ├── data/
    │   ├── domain/
    │   └── ui/
    ├── movies/
    │   ├── data/
    │   ├── domain/
    │   └── ui/
    └── people/
        ├── data/
        ├── domain/
        └── ui/
```

### Key Technologies

- **Language**: Kotlin 2.3.0
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 16)
- **Build System**: Gradle KTS with Version Catalogs (AGP 9.0.0)

### Libraries & Frameworks

| Category     | Library                            |
|--------------|------------------------------------|
| DI           | Hilt                               |
| Networking   | Retrofit + OkHttp + Moshi          |
| UI           | Material Design 3, ViewBinding, XML|
| Architecture | MVVM, StateFlow, Coroutines Flow   |
| Storage      | DataStore + Tink (encryption)      |
| Image Loading| Coil                               |
| Navigation   | Jetpack Navigation                 |
| Testing      | JUnit, MockK, Turbine              |

## Features

### 1. Login Screen
- Username validation (minimum 4 characters)
- Password validation (8+ chars, uppercase, lowercase, digit, special char)
- Session management with automatic token handling

### 2. Home Screen
- Welcome message and navigation hints
- Bottom navigation to Movies and People

### 3. Movies Screen
- Trending movies list with grid layout
- Day/Week time window filter
- Movie posters with ratings and release year
- Movie detail view with full information
- Loading and error states

### 4. People Screen
- Trending people list with linear layout
- Day/Week time window filter
- Profile photos with known works
- Person detail view with biography
- Loading and error states

## Architecture Highlights

### Clean Architecture Layers

1. **Data Layer**: DTOs, Mappers, DataSources, Repository implementations
2. **Domain Layer**: Models, Repository interfaces, Use Cases, Result wrapper
3. **UI Layer**: Fragments (XML), ViewModels (StateFlow), Adapters

### Repository & UseCase Pattern

Repositories are simple `suspend` functions that return data directly:

```kotlin
interface MoviesRepository {
    suspend fun getTrendingMovies(timeWindow: String): List<Movie>
    suspend fun getMovieDetail(movieId: Int): MovieDetail
}
```

UseCases wrap repository calls with `asResult()` to provide Loading/Success/Error states:

```kotlin
class GetTrendingMoviesUseCase(private val repository: MoviesRepository) {
    operator fun invoke(timeWindow: TimeWindow = TimeWindow.DAY): Flow<Result<List<Movie>>> = flow {
        emit(repository.getTrendingMovies(timeWindow.value))
    }.asResult()
}
```

### Result Wrapper

The `Result` sealed class in `core/domain` provides unified state handling:

```kotlin
sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val throwable: Throwable? = null) : Result<Nothing>()
}
```

The `asResult()` extension transforms any `Flow<T>` into `Flow<Result<T>>`:

```kotlin
fun <T> Flow<T>.asResult(): Flow<Result<T>> = this
    .map<T, Result<T>> { Result.Success(it) }
    .onStart { emit(Result.Loading) }
    .catch { emit(Result.Error(it.message ?: "Unknown error", it)) }
```

### Dependency Injection

Using **Hilt** for dependency injection:
- `@HiltAndroidApp` for Application class
- `@AndroidEntryPoint` for Activities and Fragments
- `@HiltViewModel` for ViewModels
- `@Inject` for constructor injection
- `@Module` + `@InstallIn` for providing dependencies

### Navigation

- **PublicActivity**: Hosts login flow
- **PrivateActivity**: Hosts authenticated content with BottomNavigationView
- Features don't know each other; navigation uses string-based routes

### Core Utilities

**ImageUrlBuilder** (`core/data`): Builds TMDB image URLs with configurable sizes:
- `buildPosterUrl()` - Movie posters (w500)
- `buildBackdropUrl()` - Backdrops (original)
- `buildProfileUrl()` - Profile images (w185)

**TimeWindow** (`core/domain`): Enum for trending time windows:
- `TimeWindow.DAY` - Daily trending
- `TimeWindow.WEEK` - Weekly trending

### Session Management

**SessionManager** (`core/domain`): Interface for managing authentication state:
- `startSession(token)` - Starts session and configures API authentication
- `logout()` - Ends session and clears credentials
- `isSessionActive()` - Checks if user is authenticated
- `setSessionCallback()` - Notifies when session expires (inactivity timeout)
- `resetInactivityTimer()` - Resets timeout on user interaction

The implementation (`SessionManagerImpl`) coordinates with `AuthInterceptor` to inject the Bearer token into all API requests automatically.

### Secure Storage

**SecureLocalDataSource** (`core/data`): Encrypted storage using Google Tink + DataStore:
- Uses **AES-GCM** encryption for sensitive data
- Stores encrypted values in **DataStore Preferences**
- Supports any serializable type via **Moshi**
- Auto-removes corrupted data on decryption failure

```kotlin
// Usage example
secureDataSource.save("username", "john_doe")
val username: String? = secureDataSource.get("username")
```

## Building & Running

### Prerequisites

1. **TMDB API Token** (required):
   - Create an account at [TMDB](https://www.themoviedb.org/)
   - Go to [API Settings](https://www.themoviedb.org/settings/api)
   - Copy the **API Read Access Token** (not the API Key)

2. **Configure local.properties**:
   ```properties
   # Copy from local.properties.example or add this line:
   TMDB_API_TOKEN=your_api_read_access_token_here
   ```

### Build & Run

1. Open the project in Android Studio (Otter or later)
2. Sync Gradle files
3. Run on emulator or device

### Test Credentials

Any username (4+ chars) and password meeting the requirements will work:
- Example: `user` / `Password1!`

## API

This app uses [TMDB API](https://api.themoviedb.org/3/) for movie and people data.

### Endpoints Used
- `GET /trending/movie/{time_window}` - Trending movies
- `GET /trending/person/{time_window}` - Trending people
- `GET /movie/{movie_id}` - Movie details
- `GET /person/{person_id}` - Person details

## Testing

The project includes comprehensive unit tests:

- **Repository tests**: Verify data mapping and data source integration
- **UseCase tests**: Verify Result state emissions (Loading → Success/Error)
- **ViewModel tests**: Verify UI state management

Testing libraries:
- **MockK**: Mocking framework for Kotlin
- **Turbine**: Testing library for Kotlin Flows

## Roadmap

- [ ] **Build Variants**: Add `offline` build variant for testing without network
- [ ] **Jetpack Compose**: Create branch with Compose UI (no Fragments)
- [ ] **Theming**: Implement dynamic theming with Material 3
- [ ] **Decorator Pattern**: Apply decorator for cross-cutting concerns (logging, caching)

## Project Conventions

- Gradle files use **KTS** format
- Dependencies managed via **Version Catalogs** (libs.versions.toml)
- XML layouts with **ViewBinding**
- Coroutines **Flow** for reactive streams
