package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.di

import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.WatchedMovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails.MovieDetailsViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.watchedmovielist.WatchedMovieListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val featureMovieCatalogPresentationModule: Module = module {

    single {
        MovieUiMapper()
    }

    single {
        WatchedMovieUiMapper()
    }

    viewModel {
        MovieListViewModel(
            getMoviesUseCase = get(),
            mapper = get(),
        )
    }

    viewModel {
        MovieDetailsViewModel(
            getMovieUseCase = get(),
            rateMovieUseCase = get(),
            saveWatchedMovieUseCase = get(),
            getWatchedMovieUseCase = get(),
            mapper = get(),
        )
    }

    viewModel {
        WatchedMovieListViewModel(
            getWatchedMoviesUseCase = get(),
            mapper = get(),
        )
    }
}
