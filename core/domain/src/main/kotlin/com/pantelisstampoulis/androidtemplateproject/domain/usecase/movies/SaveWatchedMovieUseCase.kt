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
