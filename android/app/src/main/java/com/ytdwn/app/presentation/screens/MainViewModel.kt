package com.ytdwn.app.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytdwn.app.domain.downloader.FetchStreamsUseCase
import com.ytdwn.app.domain.models.AudioStream
import com.ytdwn.app.domain.models.VideoStream
import com.ytdwn.app.presentation.components.QualityItemUiModel
import com.ytdwn.app.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val fetchStreamsUseCase: FetchStreamsUseCase = FetchStreamsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Initial)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Hold the raw extracted models
    private var rawVideoStreams = emptyList<VideoStream>()
    private var rawAudioStreams = emptyList<AudioStream>()
    
    // UI mapping
    private var videoItems = emptyList<QualityItemUiModel>()
    private var audioItems = emptyList<QualityItemUiModel>()

    fun fetchStreams(url: String) {
        if (url.isBlank()) {
            _uiState.value = MainUiState.Error("Please enter a URL")
            return
        }

        Logger.i("MainViewModel", "Initiating stream fetch for: $url")
        _uiState.value = MainUiState.Loading

        viewModelScope.launch {
            val result = fetchStreamsUseCase(url)
            result.onSuccess { extraction ->
                
                rawVideoStreams = extraction.videoStreams
                rawAudioStreams = extraction.audioStreams
                
                // Map Domain Models to UI Models
                videoItems = rawVideoStreams.map {
                    QualityItemUiModel(
                        id = it.itag,
                        title = "${it.resolution} | ${it.format}",
                        subtitle = "${it.fps} FPS | ${it.codec} | ${it.fileSizeStr}"
                    )
                }
                
                audioItems = extraction.audioStreams.map {
                    QualityItemUiModel(
                        id = it.itag,
                        title = "${it.abr} | ${it.format}",
                        subtitle = "${it.codec} | ${it.fileSizeStr}"
                    )
                }

                // Format Duration
                val durationStr = formatDuration(extraction.metadata.lengthSeconds)
                // Format Views
                val viewsStr = formatViews(extraction.metadata.views)

                _uiState.value = MainUiState.MetadataLoaded(
                    videoTitle = extraction.metadata.title,
                    channelName = extraction.metadata.author,
                    duration = durationStr,
                    uploadDate = extraction.metadata.publishDate,
                    viewCount = viewsStr,
                    thumbnailUrl = extraction.metadata.thumbnailUrl
                )
                Logger.i("MainViewModel", "Metadata successfully loaded and state updated.")

            }.onFailure { error ->
                Logger.e("MainViewModel", "Failed to fetch streams: ${error.message}", error)
                _uiState.value = MainUiState.Error(error.message ?: "An unknown error occurred while fetching streams.")
            }
        }
    }

    fun selectVideoQuality(itag: String) {
        val currentState = _uiState.value
        if (currentState is MainUiState.MetadataLoaded) {
            val selectedVideo = rawVideoStreams.find { it.itag == itag }
            if (selectedVideo != null) {
                Logger.d("MainViewModel", "Video stream selected: $itag")
                _uiState.value = currentState.copy(selectedVideo = selectedVideo)
            } else {
                Logger.e("MainViewModel", "Invalid video itag selected: $itag")
            }
        }
    }

    fun selectAudioQuality(itag: String) {
        val currentState = _uiState.value
        if (currentState is MainUiState.MetadataLoaded) {
            val selectedAudio = rawAudioStreams.find { it.itag == itag }
            if (selectedAudio != null) {
                Logger.d("MainViewModel", "Audio stream selected: $itag")
                _uiState.value = currentState.copy(selectedAudio = selectedAudio)
            } else {
                Logger.e("MainViewModel", "Invalid audio itag selected: $itag")
            }
        }
    }

    fun getAvailableVideoItems() = videoItems
    fun getAvailableAudioItems() = audioItems

    fun resetToInitial() {
        _uiState.value = MainUiState.Initial
        rawVideoStreams = emptyList()
        rawAudioStreams = emptyList()
        videoItems = emptyList()
        audioItems = emptyList()
    }

    private fun formatDuration(seconds: Long): String {
        if (seconds <= 0) return "N/A"
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }

    private fun formatViews(views: Long): String {
        if (views <= 0) return "N/A"
        return String.format("%,d", views)
    }
}
