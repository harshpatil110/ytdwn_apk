package com.ytdwn.app.presentation.screens

import com.ytdwn.app.domain.models.AudioStream
import com.ytdwn.app.domain.models.VideoStream

/**
 * Represents the different phases of the application workflow.
 * These states will control the visibility of UI sections.
 */
sealed interface MainUiState {
    
    // Initial state: Only Header, URL Input, and Footer are visible.
    data object Initial : MainUiState
    
    // Loading state: Triggered when "ENTER" is pressed. Shows loading indicators.
    data object Loading : MainUiState
    
    // Metadata Loaded state: Video Information and Quality Cards are visible.
    data class MetadataLoaded(
        // Placeholders for future data
        val videoTitle: String = "Video Information Placeholder",
        val channelName: String = "Channel Placeholder",
        val duration: String = "00:00",
        val uploadDate: String = "N/A",
        val viewCount: String = "0",
        val thumbnailUrl: String? = null,
        val selectedVideo: VideoStream? = null,
        val selectedAudio: AudioStream? = null
    ) : MainUiState

    // Downloading state: Progress Section is visible. URL Input and Download button disabled.
    data class Downloading(
        val progressPercentage: Float = 0f,
        val statusText: String = "Preparing download...",
        val downloadSpeed: String = "0 KB/s",
        val timeRemaining: String = "Calculating..."
    ) : MainUiState

    // Completed state: Shows success message.
    data object Completed : MainUiState

    // Error state: Shows error message and allows retry.
    data class Error(val message: String) : MainUiState
}
