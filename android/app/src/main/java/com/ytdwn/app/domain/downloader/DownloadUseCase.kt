package com.ytdwn.app.domain.downloader

import android.content.Context
import java.io.File

class DownloadUseCase(context: Context) {
    private val engine = DownloadEngine(context)

    suspend operator fun invoke(
        url: String,
        videoItag: String,
        audioItag: String,
        onProgress: (Float, String, String, String) -> Unit
    ): Result<Pair<File, File>> {
        return engine.downloadMedia(url, videoItag, audioItag, onProgress)
    }

    fun cleanTempFiles() {
        engine.cleanTempFiles()
    }
}
