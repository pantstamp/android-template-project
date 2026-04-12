package com.pantelisstampoulis.androidtemplateproject.domain.di

import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMovieUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMovieUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetWatchedMoviesUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.RateMovieUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SaveWatchedMovieUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.SaveWatchedMovieUseCaseImpl
import com.pantelisstampoulis.androidtemplateproject.utils.koin.getWith
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule: Module = module {

    factory {
        GetMoviesUseCaseImpl(
            moviesRepository = get(),
            coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
            logger = getWith("GetMoviesUseCase"),
        )
    } bind GetMoviesUseCase::class

    factory {
        GetMovieUseCaseImpl(
            moviesRepository = get(),
            coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
            logger = getWith("GetMoviesUseCase"),
        )
    } bind GetMovieUseCase::class

    factory {
        RateMovieUseCaseImpl(
            moviesRepository = get(),
            coroutineContext = get(qualifier = named(CoroutinesDispatchers.IO)),
            logger = getWith("GetMoviesUseCase"),
        )
    } bind RateMovieUseCase::class

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
}
