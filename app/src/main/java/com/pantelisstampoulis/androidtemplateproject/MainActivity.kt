package com.pantelisstampoulis.androidtemplateproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.pantelisstampoulis.androidtemplateproject.navigation.NavigationConstants
import com.pantelisstampoulis.androidtemplateproject.ui.screen.AndroidTemplateProjectApp
import com.pantelisstampoulis.androidtemplateproject.ui.theme.AndroidTemplateProjectTheme
import org.koin.compose.getKoin

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidTemplateProjectTheme {
                AndroidTemplateProjectApp()
            }
        }
    }
}

