package com.pantelisstampoulis.androidtemplateproject.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.navigation.MovieCatalogDestination
import com.pantelisstampoulis.androidtemplateproject.navigation.Navigator

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navigator: Navigator) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            navigator.navigateTo(MovieCatalogDestination.MovieListDestination)
        }) {
            Text(text = "Go to Movie List")
        }
    }
}