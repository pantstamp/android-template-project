package com.pantelisstampoulis.androidtemplateproject.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pantelisstampoulis.androidtemplateproject.navigation.AppNavHost
import com.pantelisstampoulis.androidtemplateproject.navigation.HomeScreenDestination

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AndroidTemplateProjectApp() {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        AppNavHost(startDestination = HomeScreenDestination)
    }
}