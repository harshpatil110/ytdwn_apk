package com.ytdwn.app.domain.downloader

import android.content.Context
import com.chaquo.python.Python
import com.ytdwn.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class DownloadEngine(private val context: Context) {

    private val tempVideoDir = File(context.cacheDir, "ytdwn_temp_video")
    private val tempAudioDir = File(context.cacheDir, "ytdwn_temp_audio")

    init {
        if (!tempVideoDir.exists()) tempVideoDir.mkdirs()
        if (!tempAudioDir.exists()) tempAudioDir.mkdirs()
    }

    suspend fun downloadMedia(
        url: String,
        videoItag: String,
        audioItag: String,
        onProgress: (Float, String, String, String) -> Unit // percent, status, speed, eta
    ): Result<Pair<File, File>> = withContext(Dispatchers.IO) {
        try {
            Logger.i("DownloadEngine", "Starting download process for URL: $url")
            val py = Python.getInstance()
            val module = py.getModule("extractor")

            val startTime = System.currentTimeMillis()
            val lastUpdateTime = AtomicLong(startTime)
            val lastBytesDownloaded = AtomicLong(0)

            val videoFile = File(tempVideoDir, "temp_video_${System.currentTimeMillis()}.mp4")
            val audioFile = File(tempAudioDir, "temp_audio_${System.currentTimeMillis()}.m4a")

            // Python Callable wrapper
            val progressCallback = object : (Long, Long) -> Unit {
                override fun invoke(bytesDownloaded: Long, totalSize: Long) {
                    val now = System.currentTimeMillis()
                    val timeDiff = now - lastUpdateTime.get()
                    
                    if (timeDiff > 500 || bytesDownloaded == totalSize) {
                        val bytesDiff = bytesDownloaded - lastBytesDownloaded.get()
                        
                        val speedBps = if (timeDiff > 0) (bytesDiff * 1000) / timeDiff else 0
                        val speedStr = formatSpeed(speedBps)
                        
                        val remainingBytes = totalSize - bytesDownloaded
                        val etaStr = if (speedBps > 0) formatEta(remainingBytes / speedBps) else "Calculating..."
                        
                        val percent = if (totalSize > 0) (bytesDownloaded.toFloat() / totalSize.toFloat()) * 100f else 0f
                        
                        lastUpdateTime.set(now)
                        lastBytesDownloaded.set(bytesDownloaded)
                        
                        onProgress(percent, "Downloading...", speedStr, etaStr)
                    }
                }
            }

            // Download Video
            Logger.i("DownloadEngine", "Downloading video itag $videoItag...")
            var videoResultStr = ""
            var videoSuccess = false
            for (attempt in 1..3) {
                onProgress(0f, "Preparing video download (Attempt $attempt/3)...", "0 KB/s", "Calculating...")
                videoResultStr = module.callAttr(
                    "download_stream", 
                    url, videoItag, tempVideoDir.absolutePath, videoFile.name, progressCallback
                ).toString()
                
                val videoResult = JSONObject(videoResultStr)
                if (videoResult.getBoolean("success")) {
                    videoSuccess = true
                    break
                }
                Logger.i("DownloadEngine", "Video download failed on attempt $attempt")
                kotlinx.coroutines.delay(1000)
            }
            
            if (!videoSuccess) {
                val err = JSONObject(videoResultStr).optString("error", "Unknown Video Error")
                Logger.e("DownloadEngine", "Video download failed after 3 attempts: $err")
                return@withContext Result.failure(Exception("Video download failed: $err"))
            }

            // Reset progress counters for audio
            lastUpdateTime.set(System.currentTimeMillis())
            lastBytesDownloaded.set(0)

            // Download Audio
            Logger.i("DownloadEngine", "Downloading audio itag $audioItag...")
            var audioResultStr = ""
            var audioSuccess = false
            for (attempt in 1..3) {
                onProgress(0f, "Preparing audio download (Attempt $attempt/3)...", "0 KB/s", "Calculating...")
                audioResultStr = module.callAttr(
                    "download_stream", 
                    url, audioItag, tempAudioDir.absolutePath, audioFile.name, progressCallback
                ).toString()
                
                val audioResult = JSONObject(audioResultStr)
                if (audioResult.getBoolean("success")) {
                    audioSuccess = true
                    break
                }
                Logger.i("DownloadEngine", "Audio download failed on attempt $attempt")
                kotlinx.coroutines.delay(1000)
            }
            
            if (!audioSuccess) {
                val err = JSONObject(audioResultStr).optString("error", "Unknown Audio Error")
                Logger.e("DownloadEngine", "Audio download failed after 3 attempts: $err")
                return@withContext Result.failure(Exception("Audio download failed: $err"))
            }

            Logger.i("DownloadEngine", "Both streams downloaded successfully.")
            onProgress(100f, "Download complete. Pending processing...", "0 KB/s", "0s")

            Result.success(Pair(videoFile, audioFile))
            
        } catch (e: Exception) {
            Logger.e("DownloadEngine", "Exception during download: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun cleanTempFiles() {
        try {
            tempVideoDir.listFiles()?.forEach { it.delete() }
            tempAudioDir.listFiles()?.forEach { it.delete() }
            Logger.i("DownloadEngine", "Temporary files cleaned.")
        } catch (e: Exception) {
            Logger.e("DownloadEngine", "Failed to clean temporary files", e)
        }
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        if (bytesPerSec < 1024) return "$bytesPerSec B/s"
        val kbps = bytesPerSec / 1024.0
        if (kbps < 1024) return String.format("%.1f KB/s", kbps)
        val mbps = kbps / 1024.0
        return String.format("%.1f MB/s", mbps)
    }

    private fun formatEta(seconds: Long): String {
        if (seconds < 60) return "${seconds}s remaining"
        val mins = seconds / 60
        val secs = seconds % 60
        return "${mins}m ${secs}s remaining"
    }
}
