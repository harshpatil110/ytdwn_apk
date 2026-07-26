package com.ytdwn.app.domain.storage

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.ytdwn.app.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class StorageManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("ytdwn_storage_prefs", Context.MODE_PRIVATE)
    
    private val PREF_CUSTOM_URI = "custom_download_uri"
    private val PREF_USE_CUSTOM = "use_custom_location"

    fun getDownloadLocationName(): String {
        if (prefs.getBoolean(PREF_USE_CUSTOM, false)) {
            val uriStr = prefs.getString(PREF_CUSTOM_URI, null)
            if (uriStr != null) {
                try {
                    val uri = Uri.parse(uriStr)
                    val docFile = DocumentFile.fromTreeUri(context, uri)
                    return docFile?.name ?: "Custom Location"
                } catch (e: Exception) {
                    Logger.e("StorageManager", "Error parsing custom URI", e)
                }
            }
        }
        return "Downloads / YTDWN"
    }

    fun setCustomLocation(uri: Uri) {
        val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        
        prefs.edit()
            .putString(PREF_CUSTOM_URI, uri.toString())
            .putBoolean(PREF_USE_CUSTOM, true)
            .apply()
        
        Logger.i("StorageManager", "Custom location set to: ${uri.toString()}")
    }

    fun resetToDefaultLocation() {
        prefs.edit().putBoolean(PREF_USE_CUSTOM, false).apply()
        Logger.i("StorageManager", "Reset to default download location.")
    }

    suspend fun saveToDestination(sourceFile: File, title: String, extension: String): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val safeName = sanitizeFilename(title)
            val fullFileName = "$safeName.$extension"

            val useCustom = prefs.getBoolean(PREF_USE_CUSTOM, false)
            if (useCustom) {
                val uriStr = prefs.getString(PREF_CUSTOM_URI, null)
                if (uriStr != null) {
                    val treeUri = Uri.parse(uriStr)
                    val docFile = DocumentFile.fromTreeUri(context, treeUri)
                    
                    if (docFile != null && docFile.canWrite()) {
                        val resultUri = saveToDocumentFile(sourceFile, docFile, fullFileName, extension)
                        return@withContext Result.success(resultUri)
                    } else {
                        Logger.i("StorageManager", "Custom location not writable. Falling back to default.")
                    }
                }
            }

            // Default location: MediaStore.Video for Videos or MediaStore.Audio for Audios
            val resultUri = saveToMediaStore(sourceFile, fullFileName, extension)
            Result.success(resultUri)

        } catch (e: Exception) {
            Logger.e("StorageManager", "Failed to save to destination: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun saveToDocumentFile(sourceFile: File, targetDir: DocumentFile, filename: String, extension: String): Uri {
        val mimeType = getMimeType(extension)
        
        var baseName = filename.substringBeforeLast(".")
        var finalName = filename
        var counter = 1
        
        // Handle duplicates in SAF
        var existingFile = targetDir.findFile(finalName)
        while (existingFile != null) {
            finalName = "${baseName}_$counter.$extension"
            existingFile = targetDir.findFile(finalName)
            counter++
        }

        val newFile = targetDir.createFile(mimeType, finalName) 
            ?: throw Exception("Could not create file in selected directory")

        copyFileToUri(sourceFile, newFile.uri)
        return newFile.uri
    }

    private fun saveToMediaStore(sourceFile: File, filename: String, extension: String): Uri {
        val resolver = context.contentResolver
        val mimeType = getMimeType(extension)
        val isVideo = mimeType.startsWith("video/")
        val isAudio = mimeType.startsWith("audio/")

        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (isVideo) android.provider.MediaStore.Video.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else if (isAudio) android.provider.MediaStore.Audio.Media.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
            else android.provider.MediaStore.Downloads.getContentUri(android.provider.MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            if (isVideo) android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else if (isAudio) android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else android.provider.MediaStore.Files.getContentUri("external")
        }

        val folderName = if (isVideo) "Movies/YTDWN" else if (isAudio) "Music/YTDWN" else "Download/YTDWN"

        val values = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, folderName)
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val itemUri = resolver.insert(collection, values) ?: throw Exception("Failed to insert into MediaStore")

        try {
            resolver.openOutputStream(itemUri)?.use { outputStream ->
                java.io.FileInputStream(sourceFile).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw Exception("Failed to open output stream to MediaStore URI")

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.clear()
                values.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)
            }
        } catch (e: Exception) {
            resolver.delete(itemUri, null, null)
            throw e
        }

        return itemUri
    }

    private fun copyFileToUri(sourceFile: File, targetUri: Uri) {
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Could not open output stream to target URI")
    }

    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "mp4" -> "video/mp4"
            "mp3" -> "audio/mpeg"
            "m4a" -> "audio/mp4"
            "webm" -> "video/webm"
            else -> "application/octet-stream"
        }
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim()
    }

    fun openFile(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val mime = context.contentResolver.getType(uri) ?: "video/*"
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with..."))
        } catch (e: Exception) {
            Logger.e("StorageManager", "Could not open file", e)
        }
    }

    fun openFolder() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val uri = Uri.parse(downloadsDir.absolutePath)
                setDataAndType(uri, "resource/folder")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Folder"))
        } catch (e: Exception) {
            Logger.e("StorageManager", "Could not open folder", e)
        }
    }
}
