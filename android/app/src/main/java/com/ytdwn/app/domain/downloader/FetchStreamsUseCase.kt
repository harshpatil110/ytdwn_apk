package com.ytdwn.app.domain.downloader

import com.ytdwn.app.data.networking.YouTubeRepository
import com.ytdwn.app.domain.models.ExtractionResult
import com.ytdwn.app.utils.Logger

class FetchStreamsUseCase(private val repository: YouTubeRepository = YouTubeRepository()) {

    suspend operator fun invoke(url: String): Result<ExtractionResult> {
        Logger.d("FetchStreamsUseCase", "Invoked with URL: $url")
        return repository.fetchStreams(url)
    }
}
