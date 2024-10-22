package com.pantelisstampoulis.androidtemplateproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pantelisstampoulis.androidtemplateproject.presentation.theme.AndroidTemplateProjectTheme
import com.pantelisstampoulis.androidtemplateproject.ui.screen.AndroidTemplateProjectApp

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
