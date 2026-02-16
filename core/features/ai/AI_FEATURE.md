# AI Assistant Feature

Voice and text-powered assistant that understands natural language commands to navigate, filter, and interact with app content using the OpenRouter API.

## Module Structure

```
core/features/ai/
├── domain/          # Pure Kotlin JVM — contracts and models
│   ├── handler/     # AIIntentHandler (abstract base)
│   ├── manager/     # AIManager, AINavigator interfaces
│   ├── model/       # AIIntent, AIIntentDescriptor, ResolvedAIIntent
│   └── repository/  # AIRepository interface
├── data/            # Android — API integration and orchestration
│   ├── di/          # Hilt modules (bindings + network)
│   ├── manager/     # AIManagerImpl (pipeline orchestrator)
│   ├── network/     # OpenRouterApiService + DTOs
│   └── repository/  # AIRepositoryImpl (prompt engineering + API calls)
└── ui/              # Android — Compose UI + speech recognition
    ├── compose/     # AIAssistant, AIFab, AIInputBar, AnimatedAIBorder
    ├── speech/      # SpeechRecognizerManager
    └── viewmodel/   # AIAssistantViewModel
```

Feature-level handlers live in their respective modules:

```
feature/movies/ui/ai/   → MoviesAIIntentHandler, MoviesAIModule
feature/people/ui/ai/   → PeopleAIIntentHandler, PeopleAIModule
feature/home/ui/ai/     → HomeAIIntentHandler, HomeAIModule
app/ai/                 → AINavigatorImpl, AINavigatorModule, NavigationObserver
```

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│                          app module                                  │
│  ┌───────────────┐  ┌──────────────────┐  ┌───────────────────────┐  │
│  │AINavigatorImpl│  │NavigationObserver│  │    PrivateActivity    │  │
│  │  (deeplinks)  │  │ (screen tracking)│  │  (wires everything)   │  │
│  └──────┬────────┘  └────────┬─────────┘  └───────────┬───────────┘  │
└─────────┼────────────────────┼────────────────────────┼──────────────┘
          │                    │                        │
          ▼                    ▼                        ▼
┌────────────────────────────────────────────────────────────────────┐
│                    core/features/ai/ui                             │
│  ┌────────────────┐  ┌──────────┐  ┌───────────┐  ┌─────────────┐  │
│  │  AIAssistant   │  │  AIFab   │  │AIInputBar │  │AnimatedAI   │  │
│  │  (root comp.)  │  │          │  │           │  │Border       │  │
│  └───────┬────────┘  └──────────┘  └───────────┘  └─────────────┘  │
│          │                                                         │
│  ┌───────▼────────────┐  ┌─────────────────────────┐               │
│  │AIAssistantViewModel│──│SpeechRecognizerManager  │               │
│  └───────┬────────────┘  └─────────────────────────┘               │
└──────────┼─────────────────────────────────────────────────────────┘
           │
           ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   core/features/ai/data                             │
│  ┌────────────────┐         ┌──────────────────┐                    │
│  │  AIManagerImpl │────────▶│ AIRepositoryImpl │                    │
│  │  (orchestrator)│         │ (prompt + API)   │                    │
│  └───────┬────────┘         └────────┬─────────┘                    │
│          │                           │                              │
│          │  ┌────────────────────────▼──────────┐                   │
│          │  │     OpenRouterApiService          │                   │
│          │  │     (Retrofit, stepfun model)     │                   │
│          │  └───────────────────────────────────┘                   │
└──────────┼──────────────────────────────────────────────────────────┘
           │
           │ Set<AIIntentHandler> (Hilt multibinding)
           │
     ┌─────┼──────────────────────────────────┐
     │     │      │               │           │
     ▼     ▼      ▼               ▼           ▼
┌────────┐ ┌────────────┐ ┌────────────┐ ┌──────────┐
│ Movies │ │   People   │ │    Home    │ │  (new    │
│Handler │ │  Handler   │ │  Handler   │ │ feature) │
└────────┘ └────────────┘ └────────────┘ └──────────┘
```

## Core Concepts

### AIIntent

Marker interface. Each feature defines a sealed interface extending it:

```kotlin
// feature/movies
sealed interface MoviesIntent : AIIntent {
    data class ChangeTimeWindow(val timeWindow: TimeWindow) : MoviesIntent
    data class ApplyFilter(val matchedIds: List<Int>, val label: String) : MoviesIntent
    data class NavigateToDetail(val movieId: Int) : MoviesIntent
    data object ClearFilter : MoviesIntent
}
```

### AIIntentDescriptor

Describes an available action to the LLM. Each handler returns a list of these:

```kotlin
AIIntentDescriptor(
    intentId = "movies_filter",
    description = "Filter displayed movies by genre, title, year, or rating",
    parameters = listOf(
        AIIntentParameter("matchedIds", "Comma-separated IDs of matching movies", "string", true),
        AIIntentParameter("filterLabel", "Short label for the filter", "string", true)
    ),
    requiredScreen = "movies"
)
```

### AIIntentHandler

Abstract base class that bridges the AI system with feature ViewModels. Responsibilities:

1. **Describe** available actions via `getDescriptors()`
2. **Resolve** LLM output into typed intents via `resolve()`
3. **Provide screen context** via `getScreenContext()` (e.g., list of visible movies with metadata)
4. **Dispatch** intents to ViewModels via `dispatch()` / `intents` Flow
5. **Synchronize** multi-step commands via `CompletableDeferred` (prepare/await/notify)

### ResolvedAIIntent

The LLM's response after interpreting a user command:

```kotlin
data class ResolvedAIIntent(
    val intentId: String,                           // e.g. "movies_filter"
    val parameters: Map<String, String>,            // e.g. {matchedIds: "1,2,3"}
    val navigationTarget: String?,                  // e.g. "movies" (for deeplink)
    val confidence: Float,
    val fallbackMessage: String?,                   // message if intent is "unknown"
    val needsFollowUp: Boolean,                     // requires context-aware 2nd pass
    val nextIntents: List<ResolvedAIIntent>         // chained preparatory steps
)
```

## Command Processing Pipeline

### Single-Step Command

```
User: "go to movies"

  ┌──────────────┐    ┌─────────────┐    ┌───────────────┐
  │ Speech /     │───▶│ AIManager   │───▶│ AIRepository  │
  │ Text input   │    │ processInput│    │ resolveIntent │
  └──────────────┘    └──────┬──────┘    └───────┬───────┘
                             │                   │
                             │    OpenRouter API │
                             │◀──────────────────┘
                             │
                             │ ResolvedAIIntent(
                             │   intentId="navigate_movies"
                             │   navigationTarget="movies"
                             │   needsFollowUp=false
                             │ )
                             │
                             ▼
                      ┌──────────────┐
                      │ executeIntent│
                      │  navigate +  │
                      │  dispatch    │
                      └──────────────┘
```

### Multi-Step Command (Intent Chaining)

For commands like _"show me romantic movies this week"_ from the home screen, the LLM returns a chain of preparatory intents in a single response, minimizing API calls:

```
User: "show me romantic movies this week"  (from home screen)

  ┌────────────────────────────────────────────────────────────────────┐
  │  LLM Call #1 — Plan + Setup                                        │
  │                                                                    │
  │  Input:  user text + all intent descriptors + current screen       │
  │  Output: intentId="navigate_movies"                                │
  │          navigationTarget="movies"                                 │
  │          needsFollowUp=true                                        │
  │          nextIntents=[{intentId="movies_trending_week"}]           │
  └──────────────────────────────┬─────────────────────────────────────┘
                                 │
                                 ▼
  ┌──────────────────────────────────────────────────────────────────────┐
  │  Execute chain sequentially                                          │
  │                                                                      │
  │  Step 1: navigate_movies                                             │
  │    → AINavigator.navigateTo("movies")                                │
  │    → prepareForScreenDataUpdate()                                    │
  │    → awaitScreenDataUpdate()  ◄── VM loads data, calls setScreenData │
  │                                                                      │
  │  Step 2: movies_trending_week                                        │
  │    → dispatch(MoviesIntent.ChangeTimeWindow(WEEK))                   │
  │    → prepareForScreenDataUpdate()                                    │
  │    → awaitScreenDataUpdate()  ◄── VM reloads, calls setScreenData    │
  └──────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  LLM Call #2 — Act (follow-up with fresh context)                  │
  │                                                                    │
  │  Input:  original user text + screen context (full movie list      │
  │          with genres, ratings, time window)                        │
  │  Output: intentId="movies_filter"                                  │
  │          parameters={matchedIds:"101,203", filterLabel:"Romantic"} │
  └──────────────────────────────┬─────────────────────────────────────┘
                                 │
                                 ▼
  ┌────────────────────────────────────────────────────────────────────┐
  │  Execute final intent                                              │
  │    → dispatch(MoviesIntent.ApplyFilter([101,203], "Romantic"))     │
  │    → VM filters displayed list, shows FilterChipBar                │
  └────────────────────────────────────────────────────────────────────┘
```

### Screen Data Synchronization

Multi-step commands require fresh data between steps. The synchronization uses `CompletableDeferred`:

```
AIManagerImpl                        AIIntentHandler                  ViewModel
     │                                    │                              │
     │ prepareForScreenDataUpdate()       │                              │
     │───────────────────────────────────▶│ creates CompletableDeferred  │
     │                                    │                              │
     │ executeIntent(step)                │                              │
     │───────────────────────────────────▶│ dispatch(intent)             │
     │                                    │─────────────────────────────▶│
     │                                    │                              │ loadData()
     │ awaitScreenDataUpdate()            │                              │   ...
     │───────────────────────────────────▶│ suspends on deferred         │
     │         (waiting...)               │                              │ setScreenData()
     │                                    │◀─────────────────────────────│
     │                                    │ notifyScreenDataLoadCompleted│
     │◀───────────────────────────────────│ deferred.complete()          │
     │ (resumes — context is fresh)       │                              │
```

## Screen Context

Handlers provide `getScreenContext()` so the LLM knows what's currently displayed. This enables filtering and detail navigation based on visible items:

```
Currently showing: trending movies this week
id=123 "Gladiator II" (2024) [Action,Adventure] rating=7.8
id=456 "Wicked" (2024) [Drama,Fantasy,Music] rating=7.6
id=789 "The Substance" (2024) [Horror,Science Fiction] rating=7.1
...
```

The LLM uses this context to:

- Match items by genre, year, rating, title, etc.
- Return specific `matchedIds` for filtering
- Return specific `movieId`/`personId` for detail navigation
- Detect if a time window change is needed before filtering

## Available Intents

### Movies

| Intent ID                | Screen       | Description                       |
|--------------------------|--------------|-----------------------------------|
| `navigate_movies`        | any          | Navigate to movies screen         |
| `movies_trending_today`  | movies       | Switch to daily trending          |
| `movies_trending_week`   | movies       | Switch to weekly trending         |
| `movies_filter`          | movies       | Filter by genre/title/year/rating |
| `movies_clear_filter`    | movies       | Clear active filter               |
| `movies_navigate_detail` | movies       | Navigate to movie detail          |
| `movies_toggle_favorite` | movie_detail | Toggle favorite                   |

### People

| Intent ID                | Screen        | Description                        |
|--------------------------|---------------|------------------------------------|
| `navigate_people`        | any           | Navigate to people screen          |
| `people_trending_today`  | people        | Switch to daily trending           |
| `people_trending_week`   | people        | Switch to weekly trending          |
| `people_filter`          | people        | Filter by department/known-for/age |
| `people_clear_filter`    | people        | Clear active filter                |
| `people_navigate_detail` | people        | Navigate to person detail          |
| `people_toggle_favorite` | person_detail | Toggle favorite                    |

### Home

| Intent ID       | Screen | Description             |
|-----------------|--------|-------------------------|
| `navigate_home` | any    | Navigate to home screen |

## Dependency Injection

Handlers are discovered automatically via Hilt multibinding — no central registry needed:

```
@Module
@InstallIn(SingletonComponent::class)
abstract class MoviesAIModule {
    @Binds @IntoSet
    abstract fun bindMoviesAIIntentHandler(impl: MoviesAIIntentHandler): AIIntentHandler
}
```

All handlers are collected into `Set<AIIntentHandler>` and injected into `AIManagerImpl`.

```
AIManagerImpl(
    handlers: Set<@JvmSuppressWildcards AIIntentHandler>,  ← auto-discovered
    aiRepository: AIRepository,
    aiNavigator: AINavigator,
    json: Json
)
```

## Pending Intent Pattern

Intents may be dispatched before the target ViewModel starts collecting (e.g., after navigation). The handler stores a `pendingIntent` that gets replayed on first collection:

```
1. AIManager dispatches intent → handler.dispatch(MoviesIntent.ApplyFilter(...))
2. Handler stores it as pendingIntent (volatile field)
3. Navigation happens → MoviesScreen created → MoviesViewModel.init()
4. ViewModel calls handler.intents.collect { ... }
5. Custom Flow emits pendingIntent first, then subscribes to MutableSharedFlow
6. ViewModel receives the intent and applies the filter
```

## UI Components

| Component          | Purpose                                                                |
|--------------------|------------------------------------------------------------------------|
| `AIAssistant`      | Root composable — wraps app content, provides FAB + input bar as slots |
| `AIFab`            | Floating action button to toggle AI mode (AutoAwesome / Close icons)   |
| `AIInputBar`       | Text field + mic button + send button with animated visibility         |
| `AnimatedAIBorder` | Rotating gradient border overlay when AI is active                     |
| `FilterChipBar`    | Shows active filter label + count + clear button (in `core/ui`)        |

## AI State Machine

```
         onFabClick()
INACTIVE ──────────────▶ READY
    ▲                      │
    │ onFabClick()         │ onMicPermissionGranted() / onSubmit()
    │                      ▼
    │                  LISTENING ──▶ PROCESSING ──▶ EXECUTING
    │                                                  │
    │              done / error                        │
    └──────────────────────────────────────────────────┘
```

## Adding AI Support to a New Feature

1. **Define intents** — sealed interface extending `AIIntent`
2. **Create handler** — extend `AIIntentHandler`:
    - `featureId` — unique identifier
    - `getDescriptors()` — list available actions
    - `resolve()` — map `ResolvedAIIntent` → feature intent
    - `getScreenContext()` — (optional) provide visible data for filtering
    - `setScreenData()` — (optional) receive data from ViewModel
3. **Create DI module** — `@Binds @IntoSet AIIntentHandler`
4. **ViewModel integration**:
    - Inject handler, collect `handler.intents` in `init`
    - Call `handler.setScreenData()` on data load success
5. **Navigation** — ensure feature has deeplink support in nav graph
6. **Add dependency** — `implementation(projects.core.features.ai.domain)` in `build.gradle.kts`

## API Configuration

- **Provider**: OpenRouter (`https://openrouter.ai/api/v1/`)
- **Model**: `stepfun/step-3.5-flash:free`
- **API key**: Loaded from `local.properties` as `OPENROUTER_API_KEY`
- **Timeout**: 60 seconds
- **Response format**: JSON with structured intent schema
