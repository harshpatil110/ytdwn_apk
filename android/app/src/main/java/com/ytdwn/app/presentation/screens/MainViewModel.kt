package com.ytdwn.app.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytdwn.app.domain.downloader.FetchStreamsUseCase
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
                
                // Map Domain Models to UI Models
                videoItems = extraction.videoStreams.map {
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
        val isAudioSelected = if (currentState is MainUiState.StreamSelection) currentState.isAudioSelected else false
        _uiState.value = MainUiState.StreamSelection(isVideoSelected = true, isAudioSelected = isAudioSelected)
    }

    fun selectAudioQuality(itag: String) {
        val currentState = _uiState.value
        val isVideoSelected = if (currentState is MainUiState.StreamSelection) currentState.isVideoSelected else false
        _uiState.value = MainUiState.StreamSelection(isVideoSelected = isVideoSelected, isAudioSelected = true)
    }

    fun getAvailableVideoItems() = videoItems
    fun getAvailableAudioItems() = audioItems

    fun resetToInitial() {
        _uiState.value = MainUiState.Initial
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
