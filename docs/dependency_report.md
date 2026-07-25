# Dependency Report

This document details every dependency used in the YTDWN desktop application and analyzes its viability for an Android migration.

## External Python Packages

### 1. pytubefix
- **Purpose**: Core library for interacting with YouTube, fetching metadata, resolving stream URLs, and downloading media chunks.
- **Location**: `core/downloader.py`, `ytdwn.py`
- **Android Support**: Yes (Pure Python).
- **Migration Strategy**: If migrating the backend using Chaquopy (Python on Android), this can remain. If migrating to a pure native Kotlin app, this must be replaced with a native library like `NewPipeExtractor` or a Kotlin YouTube scraper.

### 2. Pillow (PIL)
- **Purpose**: Image processing for downloading and displaying YouTube thumbnails in the Tkinter UI.
- **Location**: `ui/app_ui.py`
- **Android Support**: Not applicable/Needed.
- **Migration Strategy**: Remove. Android provides robust native image loading libraries (Glide, Coil, Picasso) that handle fetching, caching, and displaying images asynchronously.

## External Binaries

### 3. FFmpeg
- **Purpose**: Merging high-quality adaptive video and audio streams; converting audio streams to MP3.
- **Location**: `core/ffmpeg_utils.py`, `ytdwn.py`
- **Android Support**: Requires specific compilation for Android architectures (ARM). Standard OS subprocess calls to a bundled executable will not work easily.
- **Migration Strategy**: Replace with an Android-compatible library such as `FFmpegKit` for Android, or rewrite the merging logic to use Android's native `MediaMuxer` API.

## Python Standard Library

### 4. tkinter
- **Purpose**: The entire graphical user interface framework.
- **Location**: `ui/app_ui.py`, `main.py`
- **Android Support**: No.
- **Migration Strategy**: Must be completely discarded and replaced with Android native UI paradigms (XML Layouts or Jetpack Compose).

### 5. threading
- **Purpose**: Offloading network requests and downloads from the main UI thread to prevent freezing.
- **Location**: `ui/app_ui.py`
- **Android Support**: Python threading works in Chaquopy, but Android has strict background execution limits.
- **Migration Strategy**: Replace UI threading with Kotlin Coroutines. For long-running downloads, replace with Android `WorkManager` or Foreground Services.

### 6. os, sys, shutil, subprocess
- **Purpose**: Path resolution, checking for FFmpeg, file deletion, executing FFmpeg, OS-specific window hiding.
- **Location**: `core/helpers.py`, `core/ffmpeg_utils.py`, `ytdwn.py`
- **Android Support**: Subprocess is heavily restricted. Standard filesystem access is restricted by Scoped Storage in modern Android versions.
- **Migration Strategy**: Subprocess calls must be removed. `os.path` logic must be adapted to use Android Context-based directories (e.g., `Context.getExternalFilesDir()`) and the Storage Access Framework (SAF) or `MediaStore` API.

### 7. urllib, io, datetime, logging
- **Purpose**: Fetching thumbnails (`urllib`, `io`), formatting duration (`datetime`), and writing logs (`logging`).
- **Location**: `ui/app_ui.py`, `core/helpers.py`, `core/downloader.py`
- **Android Support**: Yes, standard Python libraries.
- **Migration Strategy**: 
  - Thumbnail fetching will be handled by Coil/Glide.
  - Duration formatting can be done in Kotlin.
  - Logging should route to Android's `Logcat` instead of a local text file.
