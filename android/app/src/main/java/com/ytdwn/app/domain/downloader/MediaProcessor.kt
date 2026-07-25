package com.ytdwn.app.domain.downloader

import android.content.Context
import android.os.Environment
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.ytdwn.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaProcessor(private val context: Context) {

    private val outputDir: File by lazy {
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            "YTDWN"
        )
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    suspend fun mergeVideoAudio(
        tempVideo: File,
        tempAudio: File,
        title: String,
        onProgress: (String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val safeTitle = sanitizeFilename(title)
            val outputFile = getUniqueFile(outputDir, safeTitle, "mp4")
            
            Logger.i("MediaProcessor", "Starting FFmpeg merge: ${outputFile.absolutePath}")
            onProgress("Merging video and audio...")

            val cmd = "-i \"${tempVideo.absolutePath}\" -i \"${tempAudio.absolutePath}\" -c:v copy -c:a aac \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(cmd)
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode)) {
                Logger.i("MediaProcessor", "Merge completed successfully.")
                Result.success(outputFile)
            } else {
                val failLogs = session.failStackTrace ?: session.logsAsString
                Logger.e("MediaProcessor", "Merge failed with return code $returnCode. Logs: $failLogs")
                Result.failure(Exception("FFmpeg merge failed. Return Code: $returnCode"))
            }
        } catch (e: Exception) {
            Logger.e("MediaProcessor", "Exception during merge: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun convertToMp3(
        tempAudio: File,
        title: String,
        onProgress: (String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val safeTitle = sanitizeFilename(title)
            val outputFile = getUniqueFile(outputDir, safeTitle, "mp3")
            
            Logger.i("MediaProcessor", "Starting MP3 conversion: ${outputFile.absolutePath}")
            onProgress("Converting audio to MP3...")

            val cmd = "-i \"${tempAudio.absolutePath}\" -q:a 0 -map a \"${outputFile.absolutePath}\""
            
            val session = FFmpegKit.execute(cmd)
            val returnCode = session.returnCode

            if (ReturnCode.isSuccess(returnCode)) {
                Logger.i("MediaProcessor", "Conversion completed successfully.")
                Result.success(outputFile)
            } else {
                val failLogs = session.failStackTrace ?: session.logsAsString
                Logger.e("MediaProcessor", "Conversion failed with return code $returnCode. Logs: $failLogs")
                Result.failure(Exception("FFmpeg conversion failed. Return Code: $returnCode"))
            }
        } catch (e: Exception) {
            Logger.e("MediaProcessor", "Exception during conversion: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
    }

    private fun getUniqueFile(directory: File, baseName: String, extension: String): File {
        var file = File(directory, "$baseName.$extension")
        var counter = 1
        while (file.exists()) {
            file = File(directory, "${baseName}_$counter.$extension")
            counter++
        }
        return file
    }
}
