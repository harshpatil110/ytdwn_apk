package com.ytdwn.app.utils

/**
 * Centralized Configuration Management.
 * All application-wide constants and feature flags should be defined here.
 */
object Configuration {
    
    // Application Info
    const val APP_NAME = "YTDWN"
    
    // Networking
    const val NETWORK_TIMEOUT_SECONDS = 30L
    
    // Storage Paths (Future definitions for MediaStore / App-Specific directories)
    const val TEMP_DIR_NAME = "ytdwn_temp"
    
    // Future Feature Flags
    const val ENABLE_FFMPEG_LOGGING = false
}
