# Implementation Plan: Watched Movies Feature

**Spec**: `SPEC.md`
**Architecture**: `CLAUDE.md`, `docs/ARCHITECTURE.md`, `docs/MODULARIZATION.md`
**Base package**: `com.pantelisstampoulis.androidtemplateproject`

---

## Design Decisions

1. **Single `MoviesRepository`** — extend the existing repository with watched movie methods. No separate repository.
2. **Keep the Rate button** — stars are interactive for selection, button tap triggers save. No change to existing UX pattern.
3. **Network + local save** — two separate use cases. `RateMovieUseCase` (existing, TMDB API) stays unchanged. New `SaveWatchedMovieUseCase` handles local DB persistence. `MovieDetailsViewModel` orchestrates: network rate on success then local save.
4. **Separate `WatchedMovieListViewModel`** — one ViewModel per tab, matching existing patterns.
5. **Separate `WatchedMovieUiModel`** — the watched tab item has different fields (user rating, no genre) than the discover item.
6. **DB migration v1 to v2** — adds `watched_movies` table.

---

## Phase 1: Data Layer

This phase builds the full vertical data stack: database abstraction, Room implementation, domain model, repository interface, repository implementation, mappers, and DI wiring. After this phase, all watched-movie data operations are functional end-to-end.

### Step 1.1 — New file: `WatchedMovieDbModel`

**Path**: `core/database/api/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/model/WatchedMovieDbModel.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.database.model

data class WatchedMovieDbModel(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
```

### Step 1.2 — Modify: `DatabaseDataSource`

**Path**: `core/database/api/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/DatabaseDataSource.kt`

Add 3 methods to the interface (keep existing methods unchanged):

```kotlin
suspend fun insertWatchedMovie(movie: WatchedMovieDbModel)
fun getWatchedMovies(): Flow<List<WatchedMovieDbModel>>
suspend fun getWatchedMovie(movieId: Int): WatchedMovieDbModel?
```

Note: `getWatchedMovies()` is NOT suspend (returns `Flow` directly for continuous observation).

### Step 1.3 — New file: `WatchedMovieEntity`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/model/WatchedMovieEntity.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watched_movies")
data class WatchedMovieEntity(
    @PrimaryKey val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
```

### Step 1.4 — New file: `WatchedMovieDao`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/dao/WatchedMovieDao.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchedMovieDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWatchedMovie(entity: WatchedMovieEntity): Long

    @Query("SELECT * FROM watched_movies ORDER BY ratedAt DESC")
    fun getWatchedMovieEntities(): Flow<List<WatchedMovieEntity>>

    @Query("SELECT * FROM watched_movies WHERE movieId = :movieId")
    fun getWatchedMovieEntity(movieId: Int): WatchedMovieEntity?
}
```

`OnConflictStrategy.ABORT` enforces immutable ratings (spec: ratings cannot be changed once saved).

### Step 1.5 — New file: `WatchedMovieDbMapper`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/mapper/WatchedMovieDbMapper.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.database.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieEntity

class WatchedMovieDbMapper {

    fun toDb(model: WatchedMovieDbModel): WatchedMovieEntity = WatchedMovieEntity(
        movieId = model.movieId,
        title = model.title,
        posterUrl = model.posterUrl,
        overview = model.overview,
        publicRating = model.publicRating,
        releaseDate = model.releaseDate,
        userRating = model.userRating,
        ratedAt = model.ratedAt,
    )

    fun mapFromDb(entity: WatchedMovieEntity): WatchedMovieDbModel = WatchedMovieDbModel(
        movieId = entity.movieId,
        title = entity.title,
        posterUrl = entity.posterUrl,
        overview = entity.overview,
        publicRating = entity.publicRating,
        releaseDate = entity.releaseDate,
        userRating = entity.userRating,
        ratedAt = entity.ratedAt,
    )
}
```

### Step 1.6 — Modify: `Mappers` (room)

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/mapper/Mappers.kt`

Add field to the existing data class:

```kotlin
val watchedMovieDbMapper: WatchedMovieDbMapper
```

### Step 1.7 — New file: `Migrations.kt`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/migration/Migrations.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `watched_movies` (
                `movieId` INTEGER NOT NULL PRIMARY KEY,
                `title` TEXT NOT NULL,
                `posterUrl` TEXT,
                `overview` TEXT,
                `publicRating` REAL NOT NULL,
                `releaseDate` TEXT,
                `userRating` INTEGER NOT NULL,
                `ratedAt` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
```

### Step 1.8 — Modify: `AppDatabase`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/AppDatabase.kt`

Changes:
- Add `WatchedMovieEntity::class` to the `entities` array.
- Bump `version` from `1` to `2`.
- Add `abstract fun watchedMovieDao(): WatchedMovieDao`.

Result:

```kotlin
@Database(
    entities = [
        MovieEntity::class,
        WatchedMovieEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun watchedMovieDao(): WatchedMovieDao
}
```

### Step 1.9 — Modify: `RoomDataSource`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/RoomDataSource.kt`

Add implementations for the 3 new `DatabaseDataSource` methods:

```kotlin
override suspend fun insertWatchedMovie(movie: WatchedMovieDbModel) {
    db.watchedMovieDao().insertWatchedMovie(mappers.watchedMovieDbMapper.toDb(movie))
}

override fun getWatchedMovies(): Flow<List<WatchedMovieDbModel>> =
    db.watchedMovieDao().getWatchedMovieEntities().map { entities ->
        entities.map { mappers.watchedMovieDbMapper.mapFromDb(it) }
    }

override suspend fun getWatchedMovie(movieId: Int): WatchedMovieDbModel? =
    db.watchedMovieDao().getWatchedMovieEntity(movieId)?.let {
        mappers.watchedMovieDbMapper.mapFromDb(it)
    }
```

### Step 1.10 — Modify: `NoopDatabaseDataSource`

**Path**: `core/database/noop/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/NoopDatabaseDataSource.kt`

Add stubs for the 3 new methods:

```kotlin
override suspend fun insertWatchedMovie(movie: WatchedMovieDbModel) { }
override fun getWatchedMovies(): Flow<List<WatchedMovieDbModel>> = flowOf(emptyList())
override suspend fun getWatchedMovie(movieId: Int): WatchedMovieDbModel? = null
```

### Step 1.11 — Modify: `DatabaseRoomModules.kt`

**Path**: `core/database/room/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/database/di/DatabaseRoomModules.kt`

Two changes:
1. In `mappersModule`, add `watchedMovieDbMapper = WatchedMovieDbMapper()` to the `Mappers` constructor call.
2. In `databaseModule`, add `.addMigrations(MIGRATION_1_2)` to the `Room.databaseBuilder(...)` chain before `.build()`.

### Step 1.12 — New file: `WatchedMovie` (domain model)

**Path**: `core/model/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/model/movies/WatchedMovie.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.model.movies

data class WatchedMovie(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
    val ratedAt: Long,
)
```

### Step 1.13 — Modify: `MoviesRepository` (interface)

**Path**: `core/domain/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/repository/MoviesRepository.kt`

Add 3 methods (keep all existing methods unchanged):

```kotlin
fun saveWatchedMovie(
    movieId: Int,
    title: String,
    posterUrl: String?,
    overview: String?,
    publicRating: Double,
    releaseDate: String?,
    userRating: Int,
): Flow<ResultState<Unit>>

fun getWatchedMovies(): Flow<ResultState<List<WatchedMovie>>>

fun getWatchedMovie(movieId: Int): Flow<ResultState<WatchedMovie>>
```

Add import for `WatchedMovie`.

### Step 1.14 — New file: `WatchedMovieDomainMapper`

**Path**: `core/data/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/data/mapper/WatchedMovieDomainMapper.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.database.model.WatchedMovieDbModel
import com.pantelisstampoulis.androidtemplateproject.mapper.DbToDomainMapper
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie

internal class WatchedMovieDomainMapper : DbToDomainMapper<WatchedMovieDbModel, WatchedMovie> {
    override fun fromDbToDomain(dbModel: WatchedMovieDbModel): WatchedMovie = WatchedMovie(
        movieId = dbModel.movieId,
        title = dbModel.title,
        posterUrl = dbModel.posterUrl,
        overview = dbModel.overview,
        publicRating = dbModel.publicRating,
        releaseDate = dbModel.releaseDate,
        userRating = dbModel.userRating,
        ratedAt = dbModel.ratedAt,
    )
}
```

### Step 1.15 — Modify: `Mappers` (data)

**Path**: `core/data/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/data/mapper/Mappers.kt`

Add field:

```kotlin
val watchedMovieDomainMapper: WatchedMovieDomainMapper
```

### Step 1.16 — Modify: `MoviesRepositoryImpl`

**Path**: `core/data/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/data/repository/MoviesRepositoryImpl.kt`

Add 3 method implementations (keep all existing methods unchanged):

```kotlin
override fun saveWatchedMovie(
    movieId: Int,
    title: String,
    posterUrl: String?,
    overview: String?,
    publicRating: Double,
    releaseDate: String?,
    userRating: Int,
): Flow<ResultState<Unit>> = flow {
    val dbModel = WatchedMovieDbModel(
        movieId = movieId,
        title = title,
        posterUrl = posterUrl,
        overview = overview,
        publicRating = publicRating,
        releaseDate = releaseDate,
        userRating = userRating,
        ratedAt = System.currentTimeMillis(),
    )
    databaseDataSource.insertWatchedMovie(dbModel)
    emit(ResultState.Success(Unit))
}

override fun getWatchedMovies(): Flow<ResultState<List<WatchedMovie>>> = flow {
    databaseDataSource.getWatchedMovies().collect { dbModels ->
        val watchedMovies = dbModels.map(mappers.watchedMovieDomainMapper::fromDbToDomain)
        emit(ResultState.Success(watchedMovies))
    }
}

override fun getWatchedMovie(movieId: Int): Flow<ResultState<WatchedMovie>> = flow {
    val dbModel = databaseDataSource.getWatchedMovie(movieId)
    dbModel?.let {
        emit(ResultState.Success(mappers.watchedMovieDomainMapper.fromDbToDomain(it)))
    } ?: emit(ResultState.Error(ErrorModel.NotFound()))
}
```

### Step 1.17 — Modify: `DataModules.kt`

**Path**: `core/data/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/data/di/DataModules.kt`

In `mappersModule`, add `watchedMovieDomainMapper = WatchedMovieDomainMapper()` to the `Mappers` constructor call.

### Verification

```bash
./gradlew assembleDebug
```

Expected: build succeeds. All new database, model, mapper, and repository code compiles. No runtime verification yet — that comes in Phase 4.

---

## Phase 2: Domain Layer

This phase adds the three new use cases and wires them into the Koin DI graph. After this phase, all business logic for saving, listing, and retrieving watched movies is available for injection.

### Step 2.1 — New file: `SaveWatchedMovieUseCase`

**Path**: `core/domain/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/SaveWatchedMovieUseCase.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface SaveWatchedMovieUseCase : UseCase<SaveWatchedMovieInput, Unit>

data class SaveWatchedMovieInput(
    val movieId: Int,
    val title: String,
    val posterUrl: String?,
    val overview: String?,
    val publicRating: Double,
    val releaseDate: String?,
    val userRating: Int,
)

internal class SaveWatchedMovieUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : SaveWatchedMovieUseCase {

    override operator fun invoke(input: SaveWatchedMovieInput): Flow<ResultState<Unit>> =
        moviesRepository.saveWatchedMovie(
            movieId = input.movieId,
            title = input.title,
            posterUrl = input.posterUrl,
            overview = input.overview,
            publicRating = input.publicRating,
            releaseDate = input.releaseDate,
            userRating = input.userRating,
        ).onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
```

### Step 2.2 — New file: `GetWatchedMoviesUseCase`

**Path**: `core/domain/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/GetWatchedMoviesUseCase.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface GetWatchedMoviesUseCase : UseCase<Unit, List<WatchedMovie>>

internal class GetWatchedMoviesUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : GetWatchedMoviesUseCase {

    override operator fun invoke(input: Unit): Flow<ResultState<List<WatchedMovie>>> =
        moviesRepository.getWatchedMovies()
            .onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
```

### Step 2.3 — New file: `GetWatchedMovieUseCase`

**Path**: `core/domain/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/GetWatchedMovieUseCase.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface GetWatchedMovieUseCase : UseCase<Int, WatchedMovie>

internal class GetWatchedMovieUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : GetWatchedMovieUseCase {

    override operator fun invoke(input: Int): Flow<ResultState<WatchedMovie>> =
        moviesRepository.getWatchedMovie(input)
            .onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
```

### Step 2.4 — Modify: `DomainModules.kt`

**Path**: `core/domain/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/di/DomainModules.kt`

Add 3 new factory bindings inside the existing `domainModule` (keep all existing bindings unchanged):

```kotlin
factory {
    SaveWatchedMovieUseCaseImpl(
        moviesRepository = get(),
        coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
        logger = getWith("SaveWatchedMovieUseCase"),
    )
} bind SaveWatchedMovieUseCase::class

factory {
    GetWatchedMoviesUseCaseImpl(
        moviesRepository = get(),
        coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
        logger = getWith("GetWatchedMoviesUseCase"),
    )
} bind GetWatchedMoviesUseCase::class

factory {
    GetWatchedMovieUseCaseImpl(
        moviesRepository = get(),
        coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
        logger = getWith("GetWatchedMovieUseCase"),
    )
} bind GetWatchedMovieUseCase::class
```

### Verification

```bash
./gradlew assembleDebug
```

Expected: build succeeds. Use cases compile and are registered in Koin. No existing code is broken.

---

## Phase 3: UI Layer

This phase modifies the movie details screen for the rating flow and adds the Watched tab to the movie list screen. After this phase, the full feature is functional end-to-end.

### Step 3.1 — Modify: `UserRatingBar`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/uicomponent/UserRatingBar.kt`

Add `enabled: Boolean = true` parameter to `UserRatingBar`. Pass it through to `StarIcon`.

In `StarIcon`, add `enabled: Boolean` parameter. When `enabled = false`, the `pointerInteropFilter` block should not update `ratingState`:

```kotlin
.pointerInteropFilter {
    if (enabled) {
        when (it.action) {
            MotionEvent.ACTION_DOWN -> {
                ratingState.value = ratingValue
            }
        }
    }
    true
}
```

### Step 3.2 — Modify: `MovieDetailsViewModel`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/moviedetails/MovieDetailsViewModel.kt`

**Constructor** — add two new parameters (keep existing ones):

```kotlin
class MovieDetailsViewModel(
    private val getMovieUseCase: GetMovieUseCase,
    private val rateMovieUseCase: RateMovieUseCase,
    private val saveWatchedMovieUseCase: SaveWatchedMovieUseCase,
    private val getWatchedMovieUseCase: GetWatchedMovieUseCase,
    private val mapper: MovieUiMapper,
) : MviViewModel<MovieDetailsEvent, MovieDetailsUiState, MovieDetailsSideEffect>(...)
```

**`MovieDetailsUiState`** — add two fields:

```kotlin
data class MovieDetailsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: MovieUiModel? = null,
    val userRating: Int? = null,
    val isRatingInProgress: Boolean = false,
) : UiState
```

**`MovieDetailsSideEffect`** — rename `ShowToast` to `ShowSnackbar`:

```kotlin
sealed interface MovieDetailsSideEffect : SideEffect {
    data class ShowSnackbar(val message: String) : MovieDetailsSideEffect
}
```

**`handleEvents` — `Init` event:**

After the existing `getMovieUseCase` collection, also launch a second coroutine to check watched status:

```kotlin
is MovieDetailsEvent.Init -> {
    // existing: load movie data
    viewModelScope.launch {
        getMovieUseCase(input = event.movieId).collect { resultState ->
            // ... existing logic unchanged ...
        }
    }
    // new: check if already rated
    viewModelScope.launch {
        getWatchedMovieUseCase(input = event.movieId).collect { resultState ->
            resultState
                .onSuccess { watchedMovie ->
                    setState { copy(userRating = watchedMovie.userRating) }
                }
                .onError {
                    // NotFound = not rated yet, leave userRating as null
                }
        }
    }
}
```

**`handleEvents` — `RateMovie` event:**

Replace existing implementation with two-step orchestration:

```kotlin
is MovieDetailsEvent.RateMovie -> {
    setState { copy(isRatingInProgress = true) }
    viewModelScope.launch {
        // Step 1: Rate on TMDB network API
        rateMovieUseCase.invoke(
            RateMovieUseCaseInput(event.movieId, event.rating),
        ).collect { resultState ->
            resultState
                .onSuccess {
                    // Step 2: Save watched movie locally
                    val movie = viewState.value.data ?: return@onSuccess
                    saveWatchedMovieUseCase.invoke(
                        SaveWatchedMovieInput(
                            movieId = movie.id,
                            title = movie.title,
                            posterUrl = movie.posterPath,
                            overview = movie.overview,
                            publicRating = movie.voteAverage,
                            releaseDate = movie.releaseYear,
                            userRating = event.rating.toInt(),
                        ),
                    ).collect { saveResult ->
                        saveResult
                            .onSuccess {
                                setState {
                                    copy(
                                        userRating = event.rating.toInt(),
                                        isRatingInProgress = false,
                                    )
                                }
                                setEffect { MovieDetailsSideEffect.ShowSnackbar("Rating saved") }
                            }
                            .onError {
                                setState { copy(isRatingInProgress = false) }
                                setEffect {
                                    MovieDetailsSideEffect.ShowSnackbar(
                                        "Something went wrong. Please try again."
                                    )
                                }
                            }
                    }
                }
                .onError {
                    setState { copy(isRatingInProgress = false) }
                    setEffect {
                        MovieDetailsSideEffect.ShowSnackbar(
                            "Something went wrong. Please try again."
                        )
                    }
                }
        }
    }
}
```

### Step 3.3 — Modify: `MovieDetailsScreen`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/moviedetails/MovieDetailsScreen.kt`

**Changes:**

1. Replace `Toast` with `Snackbar`:
   - Add `val snackbarHostState = remember { SnackbarHostState() }`.
   - Wrap the screen in `Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) })`.
   - In `ObserveEffects`, handle `ShowSnackbar` by calling `snackbarHostState.showSnackbar(sideEffect.message)` inside a coroutine scope.

2. Pass `state.userRating` and `state.isRatingInProgress` to the `RateMovie` composable.

3. Update `MovieDetails` composable signature to accept `userRating: Int?` and `isRatingInProgress: Boolean`.

4. Rewrite `RateMovie` composable:

```kotlin
@Composable
fun RateMovie(
    modifier: Modifier = Modifier,
    onEvent: (MovieDetailsEvent) -> Unit,
    movie: MovieUiModel,
    userRating: Int?,
    isRatingInProgress: Boolean,
) {
    Column(
        modifier = modifier.fillMaxWidth().wrapContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (userRating != null) {
            // Already rated — read-only display
            Text(
                text = stringResource(id = R.string.label_you_rated_this),
                style = MaterialTheme.typography.labelMedium,
            )
            val lockedRatingState = remember { mutableIntStateOf(userRating) }
            UserRatingBar(
                ratingState = lockedRatingState,
                size = 28.dp,
                enabled = false,
            )
        } else {
            // Not rated — interactive
            val ratingState = rememberSaveable { mutableIntStateOf(0) }
            UserRatingBar(
                ratingState = ratingState,
                size = 28.dp,
                enabled = !isRatingInProgress,
            )
            Button(
                onClick = {
                    onEvent(MovieDetailsEvent.RateMovie(movie.id, ratingState.intValue.toFloat()))
                },
                enabled = ratingState.intValue >= 1 && !isRatingInProgress,
            ) {
                Text(
                    text = stringResource(id = R.string.label_rate),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
```

### Step 3.4 — New file: `WatchedMovieUiModel`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/uimodel/WatchedMovieUiModel.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel

import androidx.compose.runtime.Immutable

@Immutable
data class WatchedMovieUiModel(
    val movieId: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double,
    val userRating: Int,
    val releaseYear: String,
)
```

### Step 3.5 — New file: `WatchedMovieUiMapper`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/mapper/WatchedMovieUiMapper.kt`

```kotlin
package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper

import com.pantelisstampoulis.androidtemplateproject.mapper.DomainToUiMapper
import com.pantelisstampoulis.androidtemplateproject.model.movies.WatchedMovie
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.WatchedMovieUiModel

class WatchedMovieUiMapper : DomainToUiMapper<WatchedMovie, WatchedMovieUiModel> {

    override fun fromDomainToUi(domainModel: WatchedMovie): WatchedMovieUiModel =
        WatchedMovieUiModel(
            movieId = domainModel.movieId,
            title = domainModel.title,
            posterPath = domainModel.posterUrl,
            voteAverage = domainModel.publicRating,
            userRating = domainModel.userRating,
            releaseYear = domainModel.releaseDate
                ?.split("-")
                ?.firstOrNull()
                .orEmpty(),
        )
}
```

Uses the same year-extraction logic as `MovieUiMapper`.

### Step 3.6 — New file: `WatchedMovieListViewModel`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/watchedmovielist/WatchedMovieListViewModel.kt`

This file contains the ViewModel, UiState, Event, and SideEffect classes (same pattern as `MovieListViewModel.kt` and its companion files — check whether events/side effects are in separate files or co-located, and follow the same convention):

```kotlin
package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist

import com.pantelisstampoulis.androidtemplateproject.domain.onError
import com.pantelisstampoulis.androidtemplateproject.domain.onLoading
import com.pantelisstampoulis.androidtemplateproject.domain.onSuccess
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.WatchedMovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.WatchedMovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.Event
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.MviViewModel
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.SideEffect
import com.pantelisstampoulis.androidtemplateproject.presentation.mvi.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class WatchedMovieListViewModel(
    private val getWatchedMoviesUseCase: GetWatchedMoviesUseCase,
    private val mapper: WatchedMovieUiMapper,
) : MviViewModel<WatchedMovieListEvent, WatchedMovieListUiState, WatchedMovieListSideEffect>(
    initialState = WatchedMovieListUiState(),
) {

    override fun handleEvents(event: WatchedMovieListEvent) {
        when (event) {
            is WatchedMovieListEvent.GetWatchedMovies -> {
                viewModelScope.launch {
                    getWatchedMoviesUseCase(input = Unit).collect { resultState ->
                        resultState
                            .onLoading {
                                setState { copy(isLoading = true) }
                            }
                            .onSuccess {
                                setState {
                                    copy(
                                        isLoading = false,
                                        errorMessage = null,
                                        data = it.map { mapper.fromDomainToUi(it) }
                                            .toImmutableList(),
                                    )
                                }
                            }
                            .onError { error ->
                                setState {
                                    copy(
                                        isLoading = false,
                                        errorMessage = error.message ?: "An error occurred",
                                    )
                                }
                            }
                    }
                }
            }

            is WatchedMovieListEvent.ShowMovieDetails -> setEffect {
                WatchedMovieListSideEffect.NavigateToMovieDetails(event.movieId)
            }
        }
    }
}

data class WatchedMovieListUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val data: ImmutableList<WatchedMovieUiModel>? = null,
) : UiState

sealed interface WatchedMovieListEvent : Event {
    data object GetWatchedMovies : WatchedMovieListEvent
    data class ShowMovieDetails(val movieId: Int) : WatchedMovieListEvent
}

sealed interface WatchedMovieListSideEffect : SideEffect {
    data class NavigateToMovieDetails(val movieId: Int) : WatchedMovieListSideEffect
}
```

Note: check if the existing codebase puts events/side effects in separate files per screen. If so, split them out. The exploration shows `MovieListEvent`, `MovieListSideEffect`, `MovieDetailsEvent`, `MovieDetailsSideEffect` — check their file locations and follow the same pattern.

### Step 3.7 — New file: `WatchedMovieListScreen`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/watchedmovielist/WatchedMovieListScreen.kt`

```kotlin
@Composable
fun WatchedMovieListScreen(
    state: WatchedMovieListUiState,
    effect: Flow<WatchedMovieListSideEffect>,
    onEvent: (WatchedMovieListEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
)
```

**Structure:**
- `Box` with `fillMaxSize`, centered content alignment (same pattern as `MovieListScreen`).
- When `state.isLoading`: show `CircularProgressIndicator`.
- When `state.errorMessage != null`: show `Text(state.errorMessage)`.
- When `state.data != null`:
  - If `state.data.isEmpty()`: show centered `Text(stringResource(R.string.watched_empty_state))`.
  - Else: show `LazyColumn` (NOT `PullToRefreshLazyColumn` — no pull-to-refresh per spec AC-4.4).
- `LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME)`: trigger `GetWatchedMovies` event. Uses `ON_RESUME` (not `ON_CREATE`) so the list refreshes when returning from details screen after rating.
- `ObserveEffects` for side effects — `NavigateToMovieDetails` calls `onMovieClicked(movieId)`.

**`WatchedMovieRow` composable:**

```kotlin
@Composable
fun WatchedMovieRow(
    movie: WatchedMovieUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

- `Card` with `onClick`, same height as `MovieRow` (150.dp).
- `Row`: poster image (`AsyncImage`) + `Column` with:
  - `Text(movie.title)` — `titleSmall`, max 2 lines, `overflow = TextOverflow.Ellipsis`.
  - `Row` for release year.
  - `Row` at bottom with:
    - TMDB rating: `Icon(star) + Text(movie.voteAverage)` — same style as `MovieRow`.
    - Spacer.
    - User rating: `Icon(star, tint = StarYellow) + Text(movie.userRating.toString())` with `Modifier.semantics { contentDescription = context.getString(R.string.content_description_user_rating, movie.userRating) }`.

### Step 3.8 — New file: `MovieCatalogTabbedScreen`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/MovieCatalogTabbedScreen.kt`

```kotlin
@Composable
fun MovieCatalogTabbedScreen(
    movieListState: MovieListUiState,
    movieListEffect: Flow<MovieListSideEffect>,
    onMovieListEvent: (MovieListEvent) -> Unit,
    watchedMovieListState: WatchedMovieListUiState,
    watchedMovieListEffect: Flow<WatchedMovieListSideEffect>,
    onWatchedMovieListEvent: (WatchedMovieListEvent) -> Unit,
    onMovieClicked: (Int) -> Unit,
)
```

**Implementation:**

```kotlin
val tabTitles = listOf(
    stringResource(R.string.tab_discover),
    stringResource(R.string.tab_watched),
)
var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

Column(modifier = Modifier.fillMaxSize()) {
    TabRow(selectedTabIndex = selectedTabIndex) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title) },
            )
        }
    }

    // Use Box with both screens in composition to preserve scroll state (AC-4.1).
    // Toggle visibility so the inactive tab's composable stays alive.
    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTabIndex == 0) {
            MovieListScreen(
                state = movieListState,
                effect = movieListEffect,
                onEvent = onMovieListEvent,
                onMovieClicked = onMovieClicked,
            )
        }
        if (selectedTabIndex == 1) {
            WatchedMovieListScreen(
                state = watchedMovieListState,
                effect = watchedMovieListEffect,
                onEvent = onWatchedMovieListEvent,
                onMovieClicked = onMovieClicked,
            )
        }
    }
}
```

**Scroll state preservation note (AC-4.1):** The simplest approach uses conditional composition as above. If scroll state is lost on tab switch, upgrade to `HorizontalPager(state = rememberPagerState { 2 })` which keeps both pages in memory. The ViewModel state (via `StateFlow`) persists regardless.

**Discover reload behavior (AC-4.2):** When `MovieListScreen` leaves composition on tab switch, its `LifecycleEventEffect(ON_CREATE)` coroutines are cancelled. When the user returns to Discover, `ON_CREATE` fires again and reloads data. This matches the spec: "the load is cancelled. When the user returns to Discover, the data reloads from scratch."

### Step 3.9 — Modify: `MovieCatalogNavigation.kt`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/navigation/MovieCatalogNavigation.kt`

Replace `addMovieListScreen` body:

```kotlin
private fun NavGraphBuilder.addMovieListScreen(
    onMovieClicked: (Int) -> Unit,
) {
    composable<MovieCatalogDestination.MovieListDestination> {
        val movieListViewModel = koinViewModel<MovieListViewModel>()
        val movieListState by movieListViewModel.viewState.collectAsStateWithLifecycle()

        val watchedMovieListViewModel = koinViewModel<WatchedMovieListViewModel>()
        val watchedMovieListState by watchedMovieListViewModel.viewState.collectAsStateWithLifecycle()

        MovieCatalogTabbedScreen(
            movieListState = movieListState,
            movieListEffect = movieListViewModel.effect,
            onMovieListEvent = movieListViewModel::setEvent,
            watchedMovieListState = watchedMovieListState,
            watchedMovieListEffect = watchedMovieListViewModel.effect,
            onWatchedMovieListEvent = watchedMovieListViewModel::setEvent,
            onMovieClicked = onMovieClicked,
        )
    }
}
```

Add imports for `WatchedMovieListViewModel`, `MovieCatalogTabbedScreen`.

### Step 3.10 — Modify: `FeatureMovieCatalogPresentationModule.kt`

**Path**: `feature/movie-catalog/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/di/FeatureMovieCatalogPresentationModule.kt`

Add:

```kotlin
single { WatchedMovieUiMapper() }

viewModel {
    WatchedMovieListViewModel(
        getWatchedMoviesUseCase = get(),
        mapper = get(),
    )
}
```

Update existing `MovieDetailsViewModel` factory:

```kotlin
viewModel {
    MovieDetailsViewModel(
        getMovieUseCase = get(),
        rateMovieUseCase = get(),
        saveWatchedMovieUseCase = get(),
        getWatchedMovieUseCase = get(),
        mapper = get(),
    )
}
```

### Step 3.11 — Modify: `strings.xml`

**Path**: `feature/movie-catalog/src/main/res/values/strings.xml`

Add these string resources:

```xml
<string name="tab_discover">Discover</string>
<string name="tab_watched">Watched</string>
<string name="label_you_rated_this">You rated this</string>
<string name="snackbar_rating_saved">Rating saved</string>
<string name="snackbar_rating_error">Something went wrong. Please try again.</string>
<string name="watched_empty_state">No movies rated yet. Start exploring and rate movies you\'ve watched.</string>
<string name="content_description_user_rating">Your rating: %d out of 10</string>
```

### Verification

```bash
./gradlew assembleDebug
```

Expected: full build succeeds. The feature is functional end-to-end. Manual QA can verify:
- Details screen shows interactive stars + Rate button for unrated movies.
- Tapping Rate saves via network then locally, shows "Rating saved" snackbar, locks stars.
- Re-opening a rated movie shows "You rated this" with non-interactive pre-filled stars.
- Movie list screen has Discover/Watched tabs.
- Watched tab shows rated movies with user rating, or empty state.
- Tab switching works, Discover reloads on return.

---

## Phase 4: Testing

This phase adds test doubles, unit tests for all new code, and verifies architecture rules. After this phase, the feature is fully tested and ready for review.

### Step 4.1 — Modify: `DatabaseTestDoubleFactory`

**Path**: `test/doubles/database/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/doubles/database/DatabaseTestDoubleFactory.kt`

Add factory method:

```kotlin
fun provideWatchedMovieDbModel() = WatchedMovieDbModel(
    movieId = randomInt(from = 1, until = 1000),
    title = randomString(),
    posterUrl = randomString(),
    overview = randomString(),
    publicRating = randomFloat(from = 0F, until = 10F).toDouble(),
    releaseDate = randomString(),
    userRating = randomInt(from = 1, until = 10),
    ratedAt = System.currentTimeMillis(),
)
```

### Step 4.2 — Modify: `DomainTestDoubleFactory`

**Path**: `test/doubles/model/src/main/kotlin/com/pantelisstampoulis/androidtemplateproject/doubles/model/DomainTestDoubleFactory.kt`

Add factory method:

```kotlin
fun provideWatchedMovieModel() = WatchedMovie(
    movieId = randomInt(from = 1, until = 1000),
    title = randomString(),
    posterUrl = randomString(),
    overview = randomString(),
    publicRating = randomFloat(from = 0F, until = 10F).toDouble(),
    releaseDate = randomString(),
    userRating = randomInt(from = 1, until = 10),
    ratedAt = System.currentTimeMillis(),
)
```

### Step 4.3 — Modify: `MoviesRepositoryImplTest`

**Path**: `core/data/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/data/repository/MoviesRepositoryImplTest.kt`

Add test cases for the 3 new repository methods. Follow the existing test patterns in this file (KoinTest + mock overrides + Turbine for Flow testing):

**`saveWatchedMovie`:**
- Verify `databaseDataSource.insertWatchedMovie()` is called with a `WatchedMovieDbModel` matching all input fields.
- Verify `ratedAt` is set (non-zero Long).
- Verify flow emits `ResultState.Success(Unit)`.

**`getWatchedMovies`:**
- Mock `databaseDataSource.getWatchedMovies()` to return a flow of `List<WatchedMovieDbModel>`.
- Verify result maps correctly to `List<WatchedMovie>` via `watchedMovieDomainMapper`.
- Verify flow emits `ResultState.Success(watchedMovies)`.

**`getWatchedMovie`:**
- **Found case:** Mock `databaseDataSource.getWatchedMovie(id)` to return a `WatchedMovieDbModel`. Verify `ResultState.Success(watchedMovie)`.
- **Not found case:** Mock to return `null`. Verify `ResultState.Error(ErrorModel.NotFound())`.

### Step 4.4 — New file: `SaveWatchedMovieUseCaseImplTest`

**Path**: `core/domain/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/SaveWatchedMovieUseCaseImplTest.kt`

Follow existing use case test patterns. Test:
- Delegates to `moviesRepository.saveWatchedMovie(...)` with correct parameters.
- Emits `ResultState.Loading` then `ResultState.Success` (via `onStartCatch`).
- On repository exception, emits `ResultState.Error`.

### Step 4.5 — New file: `GetWatchedMoviesUseCaseImplTest`

**Path**: `core/domain/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/GetWatchedMoviesUseCaseImplTest.kt`

Test:
- Delegates to `moviesRepository.getWatchedMovies()`.
- Emits `ResultState.Loading` then `ResultState.Success(list)`.
- On repository exception, emits `ResultState.Error`.

### Step 4.6 — New file: `GetWatchedMovieUseCaseImplTest`

**Path**: `core/domain/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/domain/usecase/movies/GetWatchedMovieUseCaseImplTest.kt`

Test:
- Delegates to `moviesRepository.getWatchedMovie(movieId)`.
- Found: emits `ResultState.Success(watchedMovie)`.
- Not found: emits `ResultState.Error(NotFound)`.

### Step 4.7 — Modify: `MovieDetailsViewModelTest`

**Path**: `feature/movie-catalog/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/moviedetails/MovieDetailsViewModelTest.kt`

Update ViewModel instantiation to include the 2 new use case mocks (`saveWatchedMovieUseCase`, `getWatchedMovieUseCase`).

Add test cases:

**Init — movie already rated:**
- Mock `getWatchedMovieUseCase(movieId)` to return `Success(watchedMovie)`.
- Verify `state.userRating == watchedMovie.userRating`.

**Init — movie not rated:**
- Mock `getWatchedMovieUseCase(movieId)` to return `Error(NotFound)`.
- Verify `state.userRating == null`.

**RateMovie — full success (network + local):**
- Mock `rateMovieUseCase` to return `Success`.
- Mock `saveWatchedMovieUseCase` to return `Success`.
- Verify `state.userRating == rating`, `state.isRatingInProgress == false`.
- Verify `ShowSnackbar("Rating saved")` effect emitted.

**RateMovie — network failure:**
- Mock `rateMovieUseCase` to return `Error`.
- Verify `state.userRating == null`, `state.isRatingInProgress == false`.
- Verify `ShowSnackbar("Something went wrong. Please try again.")` effect emitted.

**RateMovie — network success, local save failure:**
- Mock `rateMovieUseCase` to return `Success`.
- Mock `saveWatchedMovieUseCase` to return `Error`.
- Verify `state.userRating == null`, `state.isRatingInProgress == false`.
- Verify error snackbar effect emitted.

### Step 4.8 — New file: `WatchedMovieListViewModelTest`

**Path**: `feature/movie-catalog/src/test/kotlin/com/pantelisstampoulis/androidtemplateproject/feature/moviecatalog/presentation/screen/watchedmovielist/WatchedMovieListViewModelTest.kt`

Follow existing `MovieListViewModelTest` patterns. Test cases:

**GetWatchedMovies — success with data:**
- Mock `getWatchedMoviesUseCase(Unit)` to return `Success(listOf(watchedMovie1, watchedMovie2))`.
- Verify `state.data` contains mapped `WatchedMovieUiModel` items, `state.isLoading == false`.

**GetWatchedMovies — success with empty list:**
- Mock to return `Success(emptyList())`.
- Verify `state.data` is empty `ImmutableList`, `state.isLoading == false`.

**GetWatchedMovies — error:**
- Mock to return `Error(ServerError("fail"))`.
- Verify `state.errorMessage != null`, `state.isLoading == false`.

**ShowMovieDetails:**
- Send `ShowMovieDetails(movieId = 42)` event.
- Verify `NavigateToMovieDetails(movieId = 42)` side effect emitted.

### Verification

```bash
# Run all unit tests
./gradlew test

# Run Konsist architecture tests specifically
./gradlew :test:konsist:test

# Run Spotless formatting check (fix with spotlessApply if needed)
./gradlew spotlessCheck
```

Expected:
- All new and existing unit tests pass.
- Konsist rules pass — all new classes follow naming conventions (`*UseCase`, `*UseCaseImpl`, `*RepositoryImpl`), reside in correct packages (`..domain..usecase..`, `..data..repository`), and respect architectural constraints.
- Spotless formatting passes (run `./gradlew spotlessApply` first if needed).

---

## File Inventory

### New files (19)

| # | Phase | Path |
|---|---|---|
| 1 | 1 | `core/database/api/src/main/kotlin/.../database/model/WatchedMovieDbModel.kt` |
| 2 | 1 | `core/database/room/src/main/kotlin/.../database/model/WatchedMovieEntity.kt` |
| 3 | 1 | `core/database/room/src/main/kotlin/.../database/dao/WatchedMovieDao.kt` |
| 4 | 1 | `core/database/room/src/main/kotlin/.../database/mapper/WatchedMovieDbMapper.kt` |
| 5 | 1 | `core/database/room/src/main/kotlin/.../database/migration/Migrations.kt` |
| 6 | 1 | `core/model/src/main/kotlin/.../model/movies/WatchedMovie.kt` |
| 7 | 1 | `core/data/src/main/kotlin/.../data/mapper/WatchedMovieDomainMapper.kt` |
| 8 | 2 | `core/domain/src/main/kotlin/.../domain/usecase/movies/SaveWatchedMovieUseCase.kt` |
| 9 | 2 | `core/domain/src/main/kotlin/.../domain/usecase/movies/GetWatchedMoviesUseCase.kt` |
| 10 | 2 | `core/domain/src/main/kotlin/.../domain/usecase/movies/GetWatchedMovieUseCase.kt` |
| 11 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/uimodel/WatchedMovieUiModel.kt` |
| 12 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/mapper/WatchedMovieUiMapper.kt` |
| 13 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/screen/watchedmovielist/WatchedMovieListViewModel.kt` |
| 14 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/screen/watchedmovielist/WatchedMovieListScreen.kt` |
| 15 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/screen/MovieCatalogTabbedScreen.kt` |
| 16 | 4 | `core/domain/src/test/kotlin/.../domain/usecase/movies/SaveWatchedMovieUseCaseImplTest.kt` |
| 17 | 4 | `core/domain/src/test/kotlin/.../domain/usecase/movies/GetWatchedMoviesUseCaseImplTest.kt` |
| 18 | 4 | `core/domain/src/test/kotlin/.../domain/usecase/movies/GetWatchedMovieUseCaseImplTest.kt` |
| 19 | 4 | `feature/movie-catalog/src/test/kotlin/.../presentation/screen/watchedmovielist/WatchedMovieListViewModelTest.kt` |

### Modified files (21)

| # | Phase | Path |
|---|---|---|
| 1 | 1 | `core/database/api/src/main/kotlin/.../database/DatabaseDataSource.kt` |
| 2 | 1 | `core/database/room/src/main/kotlin/.../database/AppDatabase.kt` |
| 3 | 1 | `core/database/room/src/main/kotlin/.../database/RoomDataSource.kt` |
| 4 | 1 | `core/database/room/src/main/kotlin/.../database/mapper/Mappers.kt` |
| 5 | 1 | `core/database/room/src/main/kotlin/.../database/di/DatabaseRoomModules.kt` |
| 6 | 1 | `core/database/noop/src/main/kotlin/.../database/NoopDatabaseDataSource.kt` |
| 7 | 1 | `core/domain/src/main/kotlin/.../domain/repository/MoviesRepository.kt` |
| 8 | 1 | `core/data/src/main/kotlin/.../data/repository/MoviesRepositoryImpl.kt` |
| 9 | 1 | `core/data/src/main/kotlin/.../data/mapper/Mappers.kt` |
| 10 | 1 | `core/data/src/main/kotlin/.../data/di/DataModules.kt` |
| 11 | 2 | `core/domain/src/main/kotlin/.../domain/di/DomainModules.kt` |
| 12 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/uicomponent/UserRatingBar.kt` |
| 13 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/screen/moviedetails/MovieDetailsViewModel.kt` |
| 14 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/screen/moviedetails/MovieDetailsScreen.kt` |
| 15 | 3 | `feature/movie-catalog/src/main/kotlin/.../navigation/MovieCatalogNavigation.kt` |
| 16 | 3 | `feature/movie-catalog/src/main/kotlin/.../presentation/di/FeatureMovieCatalogPresentationModule.kt` |
| 17 | 3 | `feature/movie-catalog/src/main/res/values/strings.xml` |
| 18 | 4 | `test/doubles/database/src/main/kotlin/.../doubles/database/DatabaseTestDoubleFactory.kt` |
| 19 | 4 | `test/doubles/model/src/main/kotlin/.../doubles/model/DomainTestDoubleFactory.kt` |
| 20 | 4 | `core/data/src/test/kotlin/.../data/repository/MoviesRepositoryImplTest.kt` |
| 21 | 4 | `feature/movie-catalog/src/test/kotlin/.../presentation/screen/moviedetails/MovieDetailsViewModelTest.kt` |
