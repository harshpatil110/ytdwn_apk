package com.ytdwn.app.domain.models

data class VideoMetadata(
    val title: String,
    val author: String,
    val lengthSeconds: Long,
    val views: Long,
    val publishDate: String,
    val thumbnailUrl: String?
)

data class VideoStream(
    val itag: String,
    val mimeType: String,
    val format: String,
    val fileSize: Long,
    val fileSizeStr: String,
    val resolution: String,
    val fps: String,
    val codec: String
)

data class AudioStream(
    val itag: String,
    val mimeType: String,
    val format: String,
    val fileSize: Long,
    val fileSizeStr: String,
    val abr: String,
    val codec: String
)

data class ExtractionResult(
    val metadata: VideoMetadata,
    val videoStreams: List<VideoStream>,
    val audioStreams: List<AudioStream>
)
