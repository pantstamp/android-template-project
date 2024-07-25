package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCaseState
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.CoroutineContext

interface GetMoviesUseCase : UseCase<Unit, List<Movie>>

internal class GetMoviesUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : GetMoviesUseCase {

    @OptIn(ExperimentalCoroutinesApi::class)
    override operator fun invoke(input: Unit): Flow<UseCaseState<List<Movie>>> =
        moviesRepository.getMovies().mapLatest {
            UseCaseState.Success(
                data = it,
            )
        }.onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
