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
