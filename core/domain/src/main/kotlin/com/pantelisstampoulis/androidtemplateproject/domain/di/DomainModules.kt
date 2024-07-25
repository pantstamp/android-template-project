package com.pantelisstampoulis.androidtemplateproject.domain.di

import com.pantelisstampoulis.androidtemplateproject.dispatcher.CoroutinesDispatchers
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCase
import com.pantelisstampoulis.androidtemplateproject.domain.usecase.movies.GetMoviesUseCaseImpl
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
}
