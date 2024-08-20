package com.pantelisstampoulis.androidtemplateproject.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.pantelisstampoulis.androidtemplateproject.R
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation.MovieCatalogDestination
import com.pantelisstampoulis.androidtemplateproject.navigation.Navigator

@Composable
fun HomeScreen(navigator: Navigator) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.app_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navigator.navigateTo(MovieCatalogDestination.MovieListDestination)
            }) {
                Text(
                    text = stringResource(id = R.string.label_discover_movies),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(id = R.string.powered_by))
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.tmdb_logo),
                contentDescription = "logo",
                modifier = Modifier
                    .height(48.dp) // Set just the height to maintain the aspect ratio
            )
        }
    }
}
