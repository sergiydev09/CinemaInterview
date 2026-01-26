# Cinema Interview App

A modern Android application built to showcase best practices for technical interviews. This app demonstrates trending movies and people using the TMDB API.

## Architecture

This project follows **Clean Architecture** principles with a modular approach:

### Module Structure

```
CinemaInterview/
├── app/                    # Application module
│   └── navigation/        # Main NavGraph, SessionNavigator
├── core/
│   ├── data/              # Networking, session, security, image URL utilities
│   ├── domain/            # Result wrapper, Flow extensions, SessionManager
│   ├── features/          # Shared features that connect other features
│   │   └── favorites/     # Favorites management (used by movies, people, home)
│   │       ├── data/      # Repository implementation, entities
│   │       └── domain/    # Use cases, repository interface, models
│   └── ui/                # Compose components, theme, navigation utilities
│       ├── compose/       # Reusable composables (LoadingContent, ErrorContent, etc.)
│       ├── theme/         # Material 3 theme (colors, typography, shapes)
│       └── navigation/    # DeeplinkScheme, BottomNavBar
└── feature/
    ├── home/
    │   └── ui/            # HomeScreen with favorites display
    ├── login/
    │   ├── data/          # Repository implementations, data sources
    │   ├── domain/        # Use cases, repository interfaces, models
    │   └── ui/            # Screens, ViewModels, views
    ├── movies/
    │   ├── data/
    │   ├── domain/        # Models, use cases, FavoriteMovie mapper
    │   └── ui/            # MoviesScreen, MovieDetailScreen
    └── people/
        ├── data/
        ├── domain/        # Models, use cases, FavoritePerson mapper
        └── ui/            # PeopleScreen, PersonDetailScreen
```

### Key Technologies

- **Language**: Kotlin 2.3.0
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 36 (Android 16)
- **Build System**: Gradle KTS with Version Catalogs (AGP 9.0.0)

### Libraries & Frameworks

| Category       | Library                                              |
|----------------|------------------------------------------------------|
| DI             | Hilt                                                 |
| Networking     | Retrofit + OkHttp + Moshi                            |
| UI             | **Jetpack Compose** + Material Design 3              |
| Architecture   | MVVM, StateFlow, Coroutines Flow                     |
| Storage        | DataStore + Tink (encryption)                        |
| Image Loading  | Coil 3 (Compose)                                     |
| Navigation     | **Compose Navigation** (type-safe with serialization)|
| Serialization  | kotlinx-serialization-json                           |
| Testing        | JUnit, MockK, Turbine                                |

## Features

### 1. Login Screen
- Username validation (minimum 4 characters)
- Password validation (8+ chars, uppercase, lowercase, digit, special char)
- Session management with automatic token handling

### 2. Home Screen
- Welcome message with user's name
- **Favorite movies** horizontal list
- **Favorite people** horizontal list
- Bottom navigation to Movies and People

### 3. Movies Screen
- Trending movies list with LazyVerticalGrid
- Day/Week time window filter toggle
- Movie posters with ratings and release year
- Movie detail view with collapsing header
- **Favorite toggle** on detail screen
- Loading and error states

### 4. People Screen
- Trending people list with LazyColumn
- Day/Week time window filter toggle
- Profile photos with known works
- Person detail view with collapsing header
- **Favorite toggle** on detail screen
- Loading and error states

## Architecture Highlights

### Clean Architecture Layers

1. **Data Layer**: DTOs, Mappers, DataSources, Repository implementations
2. **Domain Layer**: Models, Repository interfaces, Use Cases, Result wrapper
3. **UI Layer**: Compose Screens, ViewModels (StateFlow), UI State classes

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

### State Management

UI state is managed with **StateFlow** and observed using `collectAsStateWithLifecycle()`:

```kotlin
// ViewModel
data class MoviesUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val timeWindow: TimeWindow = TimeWindow.DAY
)

private val _uiState = MutableStateFlow(MoviesUiState())
val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()
```

```kotlin
// Screen Composable
@Composable
fun MoviesScreen(viewModel: MoviesViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Render based on uiState
}
```

One-time events (navigation, snackbars) use `SharedFlow<Event>` collected in `LaunchedEffect`.

### Dependency Injection

Using **Hilt** for dependency injection:
- `@HiltAndroidApp` for Application class
- `@AndroidEntryPoint` for Activities
- `@HiltViewModel` for ViewModels (injected via `hiltViewModel()` in Compose)
- `@Inject` for constructor injection
- `@Module` + `@InstallIn` for providing dependencies

### Navigation

**Type-Safe Compose Navigation** with `kotlinx-serialization`:

- **Routes** are `@Serializable` data classes/objects for type safety:
  ```kotlin
  @Serializable data object HomeRoute
  @Serializable data class MovieDetailRoute(val movieId: Int)
  @Serializable data class PersonDetailRoute(val personId: Int)
  ```

- **Deep Linking** with custom scheme (`cinema://movies`, `cinema://movie?movieId=123`)

- **Activity Structure**:
  - **PublicActivity**: Hosts login flow
  - **PrivateActivity**: Hosts `NavHost` with `BottomNavigationBar`

- **SessionNavigator**: Handles authentication-level navigation between activities

- Features don't know each other; navigation callbacks are injected from the main NavGraph

### Compose UI Components

Reusable composables in `core/ui/compose/`:

| Component                | Description                                      |
|--------------------------|--------------------------------------------------|
| `LoadingContent`         | Centered loading spinner with optional message   |
| `ErrorContent`           | Error display with warning icon and retry button |
| `LoadingButton`          | Button with loading state and spinner            |
| `CinemaAsyncImage`       | Coil 3 AsyncImage wrapper for image loading      |
| `CollapsingHeaderLayout` | Custom layout with collapsing header animation   |
| `InlineError`            | Inline error messages for forms                  |
| `TimeWindowToggle`       | Day/Week toggle for trending filters             |
| `BottomNavBar`           | Reusable bottom navigation bar component         |

### Theme

Material 3 dark theme in `core/ui/theme/`:

- **Colors**: Dark blue primary (#1A237E), gold accent (#FFC107), dark surface (#121212)
- **Typography**: Full Material 3 type scale
- **Shapes**: Material 3 shape definitions
- **CinemaTheme**: Root composable wrapping the entire app

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

### Favorites Module

**core/features/favorites** provides local favorites management (shared across multiple features):

- **Domain**: `FavoritesRepository`, `FavoriteMovie`, `FavoritePerson` models
- **Use Cases**: `ToggleMovieFavoriteUseCase`, `TogglePersonFavoriteUseCase`
- **Data**: In-memory repository implementation with reactive `Flow` updates

```kotlin
// Toggle favorite from ViewModel
viewModelScope.launch {
    toggleMovieFavoriteUseCase(movie)
}

// Observe favorites reactively
favoritesRepository.getFavoriteMovies()
    .collect { movies -> /* update UI */ }
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
- [x] **Jetpack Compose**: Migrated to Compose UI (no Fragments)
- [x] **Theming**: Implemented Material 3 dark theme
- [x] **Favorites**: Added local favorites management
- [ ] **Decorator Pattern**: Apply decorator for cross-cutting concerns (logging, caching)
- [ ] **Persistent Favorites**: Store favorites with Room database

## Project Conventions

- Gradle files use **KTS** format
- Dependencies managed via **Version Catalogs** (libs.versions.toml)
- UI built entirely with **Jetpack Compose** (no XML layouts)
- **Compose BOM** (2026.01.00) for consistent Compose versions
- Navigation uses **type-safe routes** with `@Serializable` classes
- Coroutines **Flow** for reactive streams
- **StateFlow** for UI state management in ViewModels
