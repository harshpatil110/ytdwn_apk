package com.ytdwn.app

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.ytdwn.app.utils.Logger

/**
 * Application entry point for YTDWN.
 * Responsible for initializing global singletons, dependency injection graphs,
 * and application-wide configurations (e.g., Logging).
 */
class YtdwnApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging infrastructure
        Logger.init(isDebug = BuildConfig.DEBUG)
        Logger.d("YtdwnApplication", "Application Started")
        
        // Initialize Python runtime
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        
        // Future: Initialize Dependency Injection (e.g., Hilt/Koin)
        // Future: Initialize background work managers
    }
}
