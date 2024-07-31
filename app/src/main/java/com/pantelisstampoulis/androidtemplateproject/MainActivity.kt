package com.pantelisstampoulis.androidtemplateproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pantelisstampoulis.androidtemplateproject.ui.screen.AndroidTemplateProjectApp
import com.pantelisstampoulis.androidtemplateproject.ui.theme.AndroidTemplateProjectTheme


class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidTemplateProjectTheme {
                AndroidTemplateProjectApp()
            }
        }
    }
}

