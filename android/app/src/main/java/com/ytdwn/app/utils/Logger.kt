package com.ytdwn.app.utils

import android.util.Log

/**
 * Application-wide Logging Infrastructure.
 * Wraps android.util.Log and supports disabling debug logs in release builds.
 */
object Logger {
    private var isLoggingEnabled = false

    fun init(isDebug: Boolean) {
        isLoggingEnabled = isDebug
    }

    fun d(tag: String, message: String) {
        if (isLoggingEnabled) {
            Log.d(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message) // Info logs might be kept in release for analytics/crash reports
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e(tag, message, throwable)
    }
}
