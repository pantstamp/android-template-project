package com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.R
import com.pantelisstampoulis.androidtemplateproject.feature.moviecatalog.presentation.uimodel.MovieUiModel
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.StarYellow

/** A movie in a list: poster, title, genre, release year and public rating. Used by Discover and search. */
@Composable
fun MovieRow(movie: MovieUiModel, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(150.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            MoviePoster(posterUrl = movie.posterPath)

            Column(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween, // Ensures space between title and rating
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(id = movie.genreStringId),
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = movie.releaseYear,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End, // Aligns items to the end of the Row
                    modifier = Modifier.fillMaxWidth(), // Ensures the Row takes the full width
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = StarYellow,
                    )

                    Text(
                        text = movie.voteAverage.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
    }
}

/**
 * A fixed 2:3 poster area. The placeholder icon sits under the image: while the poster loads,
 * when it fails, or when there is none, [AsyncImage] draws nothing and the icon shows; a loaded
 * poster is opaque and covers it. Layering avoids SubcomposeAsyncImage, which Coil advises
 * against in lazy lists.
 */
@Composable
private fun MoviePoster(posterUrl: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(2f / 3f)
            .background(color = MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_movie_placeholder),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Preview
@Composable
fun PreviewMovieRowPosterPlaceholder() {
    MovieRow(
        movie = MovieUiModel(
            id = 1,
            adult = false,
            backdropPath = null,
            genreStringId = R.string.genre_science_fiction,
            originalLanguage = "en",
            originalTitle = "Interstellar",
            overview = "A team of explorers travel through a wormhole in space.",
            popularity = 100.0,
            posterPath = null,
            releaseYear = "2014",
            title = "Interstellar",
            video = false,
            voteAverage = 8.6,
            voteCount = 30000,
        ),
        onClick = {},
    )
}
