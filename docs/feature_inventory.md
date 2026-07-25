# Feature Inventory

A complete inventory of all features currently implemented in the YTDWN desktop application.

## UI Features
- **Main Window**: Scalable, custom-styled window using Stitch design tokens.
- **Scrollable Layout**: Global canvas-based scrollbar for handling dynamic content overflow.
- **URL Input Section**: Custom styled text entry and primary action button.
- **Thumbnail Display**: Asynchronous fetching and rendering of YouTube video thumbnails.
- **Metadata Display**: Visual layout for Video Title, Channel Name, Duration, Upload Date, and View Count.
- **Video Quality Cards**: Interactive list of available video streams displaying Resolution, Format, FPS, Codec, and Size.
- **Audio Quality Cards**: Interactive list of available audio streams displaying Bitrate, Format, Codec, and Size.
- **Hover & Selection States**: Custom event bindings for button hovers and card selections.
- **Download Action Panel**: Displays the destination path and provides the main download trigger.
- **Progress UI**: A horizontal progress bar tracking download percentage.
- **Status Messages**: Real-time status text indicating current operation (e.g., "Fetching streams...", "Merging Audio and Video...").
- **Dialogs**: OS-native message boxes for errors and success notifications.

## Downloader Features
- **Stream Discovery**: Extracting and categorizing available video and audio streams from a YouTube URL.
- **Progressive Download**: Downloading video streams that already contain audio (usually limited to 720p).
- **Adaptive Download**: Downloading high-resolution video streams (1080p, 4K) and audio streams separately.
- **Audio Extraction**: Downloading pure audio streams.
- **Stream Sorting**: Automatically sorting video streams by resolution and audio streams by bitrate.
- **Metadata Extraction**: Safely parsing video metadata, handling missing data gracefully.

## Media Processing Features (FFmpeg)
- **Stream Merging**: Combining adaptive video and audio tracks into a final `.mp4` container without re-encoding video.
- **Audio Conversion**: Re-encoding raw audio streams (like `webm` or `m4a`) into standard `.mp3` format.

## File Management Features
- **Path Resolution**: Determining the default user `Downloads/YouTube` directory across OS environments.
- **Directory Creation**: Automatically creating destination and logging directories if they do not exist.
- **Filename Sanitization**: Stripping illegal OS characters (`\/:*?"<>|`) from video titles to prevent file system errors.
- **Temporary Files**: Creating temporary filenames for adaptive streams and automatically deleting them post-merge.

## Error Handling
- **Missing Dependencies**: Checking for FFmpeg installation on startup and alerting the user.
- **Network Failures**: Catching exceptions during stream fetching or downloading and pushing error messages to the UI.
- **Invalid Inputs**: Warning the user if attempting to download without selecting a quality or providing a URL.
- **Fallback Metadata**: Supplying "Unknown" default values when YouTube metadata is missing.

## Performance & Threading
- **Non-blocking UI**: Executing all network and subprocess tasks on background threads.
- **Thread-safe Callbacks**: Using Tkinter's `after()` method to marshal background thread updates back to the main UI thread.
- **Progress Hook**: Stream-based chunk processing that calculates download percentage without loading the whole file in memory.

## Utility Features
- **Size Formatting**: Converting raw byte counts into human-readable strings (KB, MB, GB).
- **Duration Formatting**: Converting raw seconds into `HH:MM:SS` format.
- **MIME Type Parsing**: Translating raw MIME types (e.g., `audio/mp4`) into clean format strings (`M4A`).
- **File Logging**: Writing debug and info logs to a local `ytdwn.log` file.
