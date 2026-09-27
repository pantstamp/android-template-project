package com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies

import com.pantelisstampoulis.androidtemplateproject.domain.ResultState
import com.pantelisstampoulis.androidtemplateproject.domain.onStartCatch
import com.pantelisstampoulis.androidtemplateproject.domain.repository.MoviesRepository
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.UseCase
import com.pantelisstampoulis.androidtemplateproject.logging.Logger
import com.pantelisstampoulis.androidtemplateproject.model.movies.Movie
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

interface SearchMoviesUseCase : UseCase<String, List<Movie>>

internal class SearchMoviesUseCaseImpl(
    private val moviesRepository: MoviesRepository,
    private val coroutineContext: CoroutineContext,
    private val logger: Logger,
) : SearchMoviesUseCase {

    override operator fun invoke(input: String): Flow<ResultState<List<Movie>>> =
        moviesRepository.searchMovies(query = input)
            .onStartCatch(coroutineContext = coroutineContext, logger = logger)
}
