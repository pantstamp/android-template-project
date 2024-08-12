package com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.di

import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_list.MovieListViewModel
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.mapper.MovieUiMapper
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.screen.movie_details.MovieDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val featureMovieCatalogModule: Module = module {

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
            mapper = get(),
        )
    }
}
