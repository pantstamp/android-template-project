package com.pantelisstampoulis.androidtemplateproject.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantelisstampoulis.androidtemplateproject.navigation.AppDestination
import com.pantelisstampoulis.androidtemplateproject.navigation.AppNavHost

@Composable
fun AndroidTemplateProjectApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AppNavHost(startDestination = AppDestination.HomeScreenDestination)
        }
    }
}
