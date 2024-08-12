package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.di

import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_list.MovieListViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.presentation.screen.movie_details.MovieDetailsViewModel
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
