package com.ytdwn.app.data.networking

import com.chaquo.python.Python
import com.ytdwn.app.domain.models.AudioStream
import com.ytdwn.app.domain.models.ExtractionResult
import com.ytdwn.app.domain.models.VideoMetadata
import com.ytdwn.app.domain.models.VideoStream
import com.ytdwn.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class YouTubeRepository {

    suspend fun fetchStreams(url: String): Result<ExtractionResult> = withContext(Dispatchers.IO) {
        try {
            Logger.d("YouTubeRepository", "Validating URL: $url")
            if (!isValidYouTubeUrl(url)) {
                return@withContext Result.failure(IllegalArgumentException("Invalid YouTube URL. Please provide a valid link."))
            }

            Logger.d("YouTubeRepository", "Fetching streams via Python bridge for: $url")
            val py = Python.getInstance()
            val module = py.getModule("extractor")
            
            // Call the Python function
            val resultJsonStr = module.callAttr("fetch_streams_json", url).toString()
            val json = JSONObject(resultJsonStr)

            if (!json.getBoolean("success")) {
                val errorMsg = json.optString("error", "Unknown Python Error")
                Logger.e("YouTubeRepository", "Extraction failed: $errorMsg")
                return@withContext Result.failure(Exception(errorMsg))
            }

            Logger.d("YouTubeRepository", "Successfully fetched metadata and streams.")
            
            // Parse Metadata
            val vInfo = json.getJSONObject("video_info")
            val metadata = VideoMetadata(
                title = vInfo.optString("title", "Unknown Title"),
                author = vInfo.optString("author", "Unknown Author"),
                lengthSeconds = vInfo.optLong("length", 0L),
                views = vInfo.optLong("views", 0L),
                publishDate = vInfo.optString("publish_date", "N/A"),
                thumbnailUrl = vInfo.optString("thumbnail_url", null)
            )

            // Parse Video Streams
            val videoList = mutableListOf<VideoStream>()
            val vArray = json.getJSONArray("video")
            for (i in 0 until vArray.length()) {
                val item = vArray.getJSONObject(i)
                videoList.add(
                    VideoStream(
                        itag = item.getString("itag"),
                        mimeType = item.optString("mime_type", ""),
                        format = item.optString("format", "UNKNOWN"),
                        fileSize = item.optLong("filesize", 0L),
                        fileSizeStr = item.optString("filesize_str", "Unknown"),
                        resolution = item.optString("resolution", "N/A"),
                        fps = item.optString("fps", "N/A"),
                        codec = item.optString("codec", "N/A")
                    )
                )
            }
            Logger.d("YouTubeRepository", "Found ${videoList.size} video streams.")

            // Parse Audio Streams
            val audioList = mutableListOf<AudioStream>()
            val aArray = json.getJSONArray("audio")
            for (i in 0 until aArray.length()) {
                val item = aArray.getJSONObject(i)
                audioList.add(
                    AudioStream(
                        itag = item.getString("itag"),
                        mimeType = item.optString("mime_type", ""),
                        format = item.optString("format", "UNKNOWN"),
                        fileSize = item.optLong("filesize", 0L),
                        fileSizeStr = item.optString("filesize_str", "Unknown"),
                        abr = item.optString("abr", "N/A"),
                        codec = item.optString("codec", "N/A")
                    )
                )
            }
            Logger.d("YouTubeRepository", "Found ${audioList.size} audio streams.")

            Result.success(ExtractionResult(metadata, videoList, audioList))
            
        } catch (e: Exception) {
            Logger.e("YouTubeRepository", "Exception during fetch: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun isValidYouTubeUrl(url: String): Boolean {
        val pattern = "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$".toRegex()
        return pattern.matches(url.trim())
    }
}
