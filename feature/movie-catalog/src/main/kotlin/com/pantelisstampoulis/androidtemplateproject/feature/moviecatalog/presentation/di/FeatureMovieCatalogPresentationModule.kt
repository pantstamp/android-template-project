package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.di

import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.moviedetails.MovieDetailsViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.screen.movielist.MovieListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val featureMovieCatalogPresentationModule: Module = module {

    single {
        MovieUiMapper()
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
            mapper = get(),
        )
    }
}
