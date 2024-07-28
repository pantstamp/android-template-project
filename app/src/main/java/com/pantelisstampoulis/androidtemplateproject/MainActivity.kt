package com.pantelisstampoulis.androidtemplateproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.movie_list.MovieListScreen
import com.pantelisstampoulis.androidtemplateproject.feature.movie_catalog.movie_list.MovieListViewModel
import com.pantelisstampoulis.androidtemplateproject.ui.theme.AndroidTemplateProjectTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTemplateProjectTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    val viewModel = koinViewModel<MovieListViewModel>()
                    val state by viewModel.viewState.collectAsStateWithLifecycle()
                    MovieListScreen(
                        state = state,
                        effect = viewModel.effect,
                        onEvent = viewModel::setEvent
                    )
                }
            }
        }
    }
}