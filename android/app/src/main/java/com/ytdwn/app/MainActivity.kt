package com.ytdwn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ytdwn.app.presentation.theme.YTDWNTheme

/**
 * Main entry point for the UI.
 * Configures the Jetpack Compose root layout and navigation foundation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Transition from splash theme to normal theme
        setTheme(R.style.Theme_YTDWN)
        super.onCreate(savedInstanceState)
        
        setContent {
            YTDWNTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Future: Replace with NavHost
                    com.ytdwn.app.presentation.screens.MainScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
