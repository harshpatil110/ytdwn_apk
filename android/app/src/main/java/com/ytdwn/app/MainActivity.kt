package com.ytdwn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ytdwn.app.utils.Logger
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ytdwn.app.presentation.screens.MainScreen
import com.ytdwn.app.presentation.theme.YTDWNTheme

/**
 * Main entry point for the UI.
 * Configures the Jetpack Compose root layout and navigation foundation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen BEFORE super.onCreate().
        // This is REQUIRED on Android 12+ (API 31+) to properly dismiss
        // the system-managed splash screen window. Without this call,
        // the splash screen overlay remains at z=30000 permanently,
        // covering the entire Compose UI with a black layer.
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        Logger.d("MainActivity", "onCreate called")
        
        setContent {
            YTDWNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
