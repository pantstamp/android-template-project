package com.pantelisstampoulis.androidtemplateproject.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The `movie/{movie_id}` response. It differs from [MovieApiModel], the list item returned by
 * `discover/movie` and `search/movie`: genres arrive as `genres: [{id, name}]` rather than
 * `genre_ids`.
 */
@Serializable
data class MovieDetailsApiModel(
    @SerialName("adult") val adult: Boolean,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("genres") val genres: List<GenreApiModel> = emptyList(),
    @SerialName("id") val id: Int,
    @SerialName("original_language") val originalLanguage: String,
    @SerialName("original_title") val originalTitle: String,
    @SerialName("overview") val overview: String = "",
    @SerialName("popularity") val popularity: Double,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("title") val title: String,
    @SerialName("video") val video: Boolean,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
)
