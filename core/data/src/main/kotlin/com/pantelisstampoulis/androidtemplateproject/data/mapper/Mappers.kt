package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieDataMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieDetailsDomainMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieDomainMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieSearchDomainMapper

internal class Mappers(
    val movieDataMapper: MovieDataMapper,
    val movieDomainMapper: MovieDomainMapper,
    val errorClassifier: ErrorClassifier,
    val watchedMovieDomainMapper: WatchedMovieDomainMapper,
    val movieSearchDomainMapper: MovieSearchDomainMapper,
    val movieDetailsDomainMapper: MovieDetailsDomainMapper,
)
