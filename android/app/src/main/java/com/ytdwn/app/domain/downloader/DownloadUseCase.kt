package com.ytdwn.app.domain.downloader

import android.content.Context
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DownloadUseCase(context: Context) {
    private val engine = DownloadEngine(context)
    private val processor = MediaProcessor(context)

    suspend operator fun invoke(
        url: String,
        videoItag: String,
        audioItag: String,
        title: String,
        onProgress: (Float, String, String, String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadResult = engine.downloadMedia(url, videoItag, audioItag, onProgress)
        
        if (downloadResult.isSuccess) {
            val (tempVideo, tempAudio) = downloadResult.getOrThrow()
            
            // For now, default to MP4 merge. We can parameterize this later if MP3 is selected.
            onProgress(100f, "Merging video and audio...", "0 KB/s", "Processing")
            val processResult = processor.mergeVideoAudio(tempVideo, tempAudio, title) { status ->
                onProgress(100f, status, "0 KB/s", "Processing")
            }
            
            // Clean temp files ONLY on success, or always? 
            // "Cleanup should occur only after successful processing. If processing fails: Preserve files for debugging"
            if (processResult.isSuccess) {
                engine.cleanTempFiles()
                onProgress(100f, "Cleaning temporary files...", "0 KB/s", "Finalizing")
                return@withContext processResult
            } else {
                return@withContext Result.failure(processResult.exceptionOrNull() ?: Exception("Unknown processing error"))
            }
            
        } else {
            return@withContext Result.failure(downloadResult.exceptionOrNull() ?: Exception("Unknown download error"))
        }
    }

    fun cleanTempFiles() {
        engine.cleanTempFiles()
    }
}
