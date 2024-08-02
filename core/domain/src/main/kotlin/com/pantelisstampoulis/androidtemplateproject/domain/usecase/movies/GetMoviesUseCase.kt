package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface GetMoviesUseCase : UseCase<Unit, List<Movie>>

internal class GetMoviesUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : GetMoviesUseCase {

    override operator fun invoke(input: Unit): Flow<ResultState<List<Movie>>> =
        moviesRepository.getMovies()
            .onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
