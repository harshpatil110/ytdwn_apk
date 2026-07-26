package com.ytdwn.app.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ytdwn.app.presentation.components.DownloadSection
import com.ytdwn.app.presentation.components.FooterSection
import com.ytdwn.app.presentation.components.HeaderSection
import com.ytdwn.app.presentation.components.ProgressSection
import com.ytdwn.app.presentation.components.QualityItemUiModel
import com.ytdwn.app.presentation.components.QualitySection
import com.ytdwn.app.presentation.components.UrlInputSection
import com.ytdwn.app.presentation.components.VideoInfoSection
import com.ytdwn.app.utils.Configuration
import com.ytdwn.app.utils.Logger

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Logger.d("MainScreen", "Entering RootComposable")
    val uiState by viewModel.uiState.collectAsState()
    val downloadLocation by viewModel.downloadLocation.collectAsState()
    
    var url by remember { mutableStateOf("") }
    
    val scrollState = rememberScrollState()

    val documentTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            viewModel.setCustomLocation(uri)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Section 1: Header (Always visible)
        Logger.d("MainScreen", "Entering Header")
        HeaderSection(modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red))

        // Section 2: URL Input (Always visible, disabled during download)
        Logger.d("MainScreen", "Entering URLSection")
        UrlInputSection(
            modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
            url = url,
            onUrlChange = { url = it },
            onEnterClick = {
                viewModel.fetchStreams(url)
            },
            enabled = uiState !is MainUiState.Downloading
        )

        // Show loading state placeholder
        if (uiState is MainUiState.Loading) {
            ProgressSection(
                modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                progress = 0f,
                statusText = "Fetching streams and metadata...",
                speed = "",
                timeRemaining = ""
            )
        }

        // Show error message if any
        if (uiState is MainUiState.Error) {
            val errorState = uiState as MainUiState.Error
            Column(
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.resetToInitial() },
                    enabled = true
                ) {
                    Text("RETRY / RESET")
                }
            }
        }

        // Sections 3, 4, 5, 6: Metadata, Qualities, and Download
        if (uiState is MainUiState.MetadataLoaded || uiState is MainUiState.Downloading || uiState is MainUiState.Completed) {
            val meta = if (uiState is MainUiState.MetadataLoaded) {
                uiState as MainUiState.MetadataLoaded
            } else {
                MainUiState.MetadataLoaded() // Fallback to placeholder if in another state
            }

            // Section 3: Video Information
            VideoInfoSection(
                modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                title = meta.videoTitle,
                channel = meta.channelName,
                duration = meta.duration,
                uploadDate = meta.uploadDate,
                views = meta.viewCount,
                thumbnailUrl = meta.thumbnailUrl
            )

            // Section 4 & 5: Qualities (Hide during download to save space)
            if (uiState !is MainUiState.Downloading) {
                val videoItems = viewModel.getAvailableVideoItems()
                val audioItems = viewModel.getAvailableAudioItems()

                QualitySection(
                    modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                    title = "AVAILABLE VIDEO QUALITIES",
                    items = videoItems,
                    selectedId = meta.selectedVideo?.itag,
                    onItemSelected = { 
                        viewModel.selectVideoQuality(it)
                    }
                )

                QualitySection(
                    modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                    title = "AVAILABLE AUDIO QUALITIES",
                    items = audioItems,
                    selectedId = meta.selectedAudio?.itag,
                    onItemSelected = { 
                        viewModel.selectAudioQuality(it)
                    }
                )
            }

            // Section 6: Download Section (shown as long as metadata is loaded or downloading or completed)
            val isCompleted = uiState is MainUiState.Completed
            val savedUri = if (isCompleted) (uiState as MainUiState.Completed).savedUri else null
            
            Logger.d("MainScreen", "Entering DownloadSection")
            DownloadSection(
                modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                downloadPath = downloadLocation,
                onDownloadClick = {
                    viewModel.startDownload(url)
                },
                onChangeLocationClick = {
                    documentTreeLauncher.launch(null)
                },
                onOpenFileClick = if (savedUri != null) { { viewModel.openCompletedFile(savedUri) } } else null,
                onOpenFolderClick = { viewModel.openFolder() },
                enabled = uiState is MainUiState.MetadataLoaded && meta.selectedVideo != null && meta.selectedAudio != null,
                isCompleted = isCompleted
            )
        }

        // Section 7: Progress Section
        if (uiState is MainUiState.Downloading) {
            val downloadState = uiState as MainUiState.Downloading
            ProgressSection(
                modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red),
                progress = downloadState.progressPercentage,
                statusText = downloadState.statusText,
                speed = downloadState.downloadSpeed,
                timeRemaining = downloadState.timeRemaining
            )
        }

        // Section 8: Footer (Always visible)
        FooterSection(modifier = Modifier.border(2.dp, androidx.compose.ui.graphics.Color.Red))
    }
}
