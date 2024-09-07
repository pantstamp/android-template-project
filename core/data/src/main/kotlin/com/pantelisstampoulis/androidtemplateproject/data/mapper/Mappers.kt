package com.pantelisstampoulis.androidtemplateproject.data.mapper

import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieDataMapper
import com.pantelisstampoulis.androidtemplateproject.data.mapper.movie.MovieDomainMapper

internal class Mappers(
    val movieDataMapper: MovieDataMapper,
    val movieDomainMapper: MovieDomainMapper,
    val errorDomainMapper: ErrorDomainMapper,
)
