# Architecture

## Overview
YTDWN is a modular desktop application designed for downloading YouTube videos and audio. The application follows a clear separation of concerns, splitting the user interface from the core downloading and processing logic. 

## Folder Hierarchy
```text
ytdwn_apk/
│
├── main.py                 # Application entry point
├── ytdwn.py                # Legacy CLI script
├── requirements.txt        # Python dependencies
├── stitch_ui.html          # UI Mockup / Reference
│
├── core/                   # Backend Logic
│   ├── downloader.py       # Core Downloader class for handling streams and downloads
│   ├── ffmpeg_utils.py     # FFmpeg subprocess wrappers for media merging/conversion
│   └── helpers.py          # Utility functions (paths, logging, formatting)
│
└── ui/                     # Presentation Layer
    └── app_ui.py           # Tkinter GUI implementation (Main window, widgets)
```

## Module Relationships & Import Graph
- `main.py` initializes the application by importing `YouTubeDownloaderApp` from `ui.app_ui`.
- `ui.app_ui` manages the UI state and user interactions. It delegates background work by importing `Downloader` from `core.downloader` and `helpers` from `core.helpers`.
- `core.downloader` depends on `pytubefix` for stream interaction, `core.helpers` for sanitization and extraction, and `core.ffmpeg_utils` for media post-processing.

## Core Architectures

### UI Architecture
The presentation layer is built exclusively with `Tkinter`. 
- **Main Window**: A scrollable Canvas containing a main Frame.
- **Custom Widgets**: Reusable UI components like `FlatButton`, `FlatEntry`, and `StitchQualityCard` abstract raw Tkinter widgets to apply consistent styling (Stitch Design Tokens).
- **State Management**: Tkinter variable classes (`tk.StringVar`, `tk.DoubleVar`) bind UI state directly to widgets.
- **Responsiveness**: Heavy blocking operations (fetching, downloading) are offloaded to background threads using the `threading` module, communicating back to the main UI thread via `after(0, callback)`.

### Backend & Downloader Architecture
The `Downloader` class in `core/downloader.py` encapsulates all YouTube interactions.
- **Metadata Fetching**: Retrieves video title, duration, thumbnail URL, and views.
- **Stream Discovery**: Iterates over available `pytubefix` streams, filtering and sorting adaptive video (by resolution) and audio streams (by bitrate).
- **Download Execution**: Supports progressive downloads (video+audio in one) or adaptive downloads (downloading video and audio streams separately).
- **Progress Tracking**: Uses a hook to calculate download percentage and invoke a callback to the UI.

### FFmpeg Architecture
Media processing is handled via standard `subprocess` calls in `core/ffmpeg_utils.py`.
- **Merging**: Combines adaptive video streams (e.g., high-res without audio) and audio streams into a final `.mp4` container.
- **Conversion**: Converts downloaded webm/m4a audio streams into `.mp3` format.

## Data Flow
1. **User Input**: User enters a URL and clicks "Enter".
2. **Metadata Fetch**: UI spawns a thread calling `Downloader.fetch_streams()`.
3. **Stream Discovery**: `pytubefix` parses the URL; `Downloader` structures available qualities and invokes the UI callback.
4. **Selection**: UI renders cards. User selects a format and clicks "Download".
5. **Download**: UI spawns a thread calling `Downloader.download_video()` or `download_mp3()`.
6. **Progress**: Chunks are downloaded, triggering the progress hook which updates the Tkinter `Progressbar`.
7. **FFmpeg Merge**: Once streams are downloaded, `subprocess.run` invokes FFmpeg to merge or convert files.
8. **Completion**: Temporary files are deleted, and the UI is notified of success.
