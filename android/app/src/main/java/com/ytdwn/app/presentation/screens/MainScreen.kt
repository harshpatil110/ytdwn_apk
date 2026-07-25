package com.ytdwn.app.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ytdwn.app.presentation.components.DownloadSection
import com.ytdwn.app.presentation.components.FooterSection
import com.ytdwn.app.presentation.components.HeaderSection
import com.ytdwn.app.presentation.components.ProgressSection
import com.ytdwn.app.presentation.components.QualityItemUiModel
import com.ytdwn.app.presentation.components.QualitySection
import com.ytdwn.app.presentation.components.UrlInputSection
import com.ytdwn.app.presentation.components.VideoInfoSection
import com.ytdwn.app.utils.Configuration

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    // For now, state is hoisted here as placeholders. 
    // In the future, this will be provided by a ViewModel.
    var uiState by remember { mutableStateOf<MainUiState>(MainUiState.Initial) }
    var url by remember { mutableStateOf("") }
    var selectedVideoId by remember { mutableStateOf<String?>(null) }
    var selectedAudioId by remember { mutableStateOf<String?>(null) }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Header (Always visible)
        HeaderSection()

        // Section 2: URL Input (Always visible, disabled during download)
        UrlInputSection(
            url = url,
            onUrlChange = { url = it },
            onEnterClick = {
                // Simulate state transition for demonstration
                uiState = MainUiState.Loading
                // Mock transition to MetadataLoaded
                uiState = MainUiState.MetadataLoaded()
            },
            enabled = uiState !is MainUiState.Downloading
        )

        // Show loading state placeholder
        if (uiState is MainUiState.Loading) {
            ProgressSection(
                progress = 0f,
                statusText = "Fetching streams...",
                speed = "",
                timeRemaining = ""
            )
        }

        // Sections 3, 4, 5: Metadata & Qualities
        if (uiState is MainUiState.MetadataLoaded || uiState is MainUiState.StreamSelection || uiState is MainUiState.Downloading) {
            val meta = if (uiState is MainUiState.MetadataLoaded) {
                uiState as MainUiState.MetadataLoaded
            } else {
                MainUiState.MetadataLoaded() // Fallback to placeholder if in another state
            }

            // Section 3: Video Information
            VideoInfoSection(
                title = meta.videoTitle,
                channel = meta.channelName,
                duration = meta.duration,
                uploadDate = meta.uploadDate,
                views = meta.viewCount
            )

            // Section 4 & 5: Qualities (Hide during download to save space)
            if (uiState !is MainUiState.Downloading) {
                // Mock Data
                val videoItems = listOf(
                    QualityItemUiModel("v1", "1080p | MP4", "60 FPS | avc1 | 150 MB"),
                    QualityItemUiModel("v2", "720p | MP4", "30 FPS | avc1 | 80 MB")
                )
                val audioItems = listOf(
                    QualityItemUiModel("a1", "160kbps | WEBM", "opus | 5 MB"),
                    QualityItemUiModel("a2", "128kbps | M4A", "mp4a | 4 MB")
                )

                QualitySection(
                    title = "AVAILABLE VIDEO QUALITIES",
                    items = videoItems,
                    selectedId = selectedVideoId,
                    onItemSelected = { 
                        selectedVideoId = it
                        uiState = MainUiState.StreamSelection(isVideoSelected = true, isAudioSelected = selectedAudioId != null)
                    }
                )

                QualitySection(
                    title = "AVAILABLE AUDIO QUALITIES",
                    items = audioItems,
                    selectedId = selectedAudioId,
                    onItemSelected = { 
                        selectedAudioId = it 
                        uiState = MainUiState.StreamSelection(isVideoSelected = selectedVideoId != null, isAudioSelected = true)
                    }
                )
            }
        }

        // Section 6: Download Section
        if (uiState is MainUiState.StreamSelection || uiState is MainUiState.Downloading || uiState is MainUiState.Completed) {
            DownloadSection(
                downloadPath = "/storage/emulated/0/Movies/YouTube", // Placeholder for SAF
                onDownloadClick = {
                    uiState = MainUiState.Downloading(progressPercentage = 45f)
                },
                enabled = uiState is MainUiState.StreamSelection
            )
        }

        // Section 7: Progress Section
        if (uiState is MainUiState.Downloading) {
            val downloadState = uiState as MainUiState.Downloading
            ProgressSection(
                progress = downloadState.progressPercentage,
                statusText = downloadState.statusText,
                speed = downloadState.downloadSpeed,
                timeRemaining = downloadState.timeRemaining
            )
        }

        Spacer(modifier = Modifier.weight(1f, fill = false))

        // Section 8: Footer (Always visible)
        FooterSection()
    }
}
