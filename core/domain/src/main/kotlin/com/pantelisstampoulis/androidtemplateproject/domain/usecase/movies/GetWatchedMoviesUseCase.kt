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
