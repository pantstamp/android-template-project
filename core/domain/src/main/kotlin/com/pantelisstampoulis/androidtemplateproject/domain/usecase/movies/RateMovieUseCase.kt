package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface RateMovieUseCase : UseCase<RateMovieUseCaseInput, Unit>

data class RateMovieUseCaseInput(val movieId: Int, val rating: Float)

internal class RateMovieUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : RateMovieUseCase {

    override operator fun invoke(input: RateMovieUseCaseInput): Flow<ResultState<Unit>> =
        moviesRepository.rateMovie(input.movieId, input.rating)
            .onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
