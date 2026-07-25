# Android Architecture Design

This document outlines the proposed architecture for the Android version of YTDWN, following Clean Architecture principles to ensure maintainability and separation of concerns.

## Architectural Layers

### 1. Presentation Layer (UI)
- **Framework**: Jetpack Compose (Modern native Android UI).
- **Responsibilities**: Rendering UI, capturing user input, displaying states.
- **Components**:
  - `MainActivity`: Entry point.
  - `Screens`: `MainScreen` (Input, Quality Selection, Progress).
  - `Components`: `QualityCard`, `CustomTextField`, `PrimaryButton`.
  - `State`: State hoisted in ViewModels using `StateFlow`.

### 2. Application Layer (ViewModels)
- **Framework**: Android Architecture Components (ViewModel, Kotlin Coroutines).
- **Responsibilities**: Managing UI state, coordinating domain use cases, handling threading (Dispatchers).
- **Components**:
  - `MainViewModel`: Exposes flows for `VideoState`, `DownloadProgress`, and `ErrorState`. Coroutine scopes manage background work lifecycles.

### 3. Business Logic Layer (Use Cases)
- **Responsibilities**: Encapsulating core app functionality. Single responsibility classes.
- **Components**:
  - `FetchMetadataUseCase`: Triggers the extraction engine.
  - `DownloadVideoUseCase`: Coordinates downloading and subsequent merging.
  - `DownloadAudioUseCase`: Coordinates downloading and audio conversion.

### 4. Download Engine & Extraction Layer
- **Responsibilities**: Interacting with YouTube to resolve streams and download chunks.
- **Implementation Options**:
  - **Option A (Python Bridge)**: Chaquopy wrapping the existing `pytubefix` logic.
  - **Option B (Native)**: Using a native Android library like `NewPipeExtractor` or rewriting core logic in Kotlin.

### 5. Media Processing Layer
- **Responsibilities**: Merging video/audio tracks and formatting audio.
- **Components**:
  - `FFmpegWrapper`: Interfaces with `FFmpegKit` for Android. Translates core application commands into FFmpeg string arguments and observes execution callbacks.

### 6. Platform Services (OS Integration)
- **Networking**: `OkHttp` or `Retrofit` for fetching thumbnails or supplementary API data.
- **Storage**: `StorageRepository` utilizing Android `MediaStore` API to save output files securely to the user's public Videos/Audio directories without requesting legacy storage permissions.
- **Background Work**: Android `WorkManager` or a `ForegroundService` with a persistent notification to ensure downloads continue if the app is minimized.

## Data Flow & Communication

1. **User Input**: `MainScreen` captures URL and calls `MainViewModel.fetchStreams(url)`.
2. **Execution**: `ViewModel` launches a coroutine (`Dispatchers.IO`) and calls `FetchMetadataUseCase`.
3. **Extraction**: UseCase invokes the Extraction Layer, returning a structured `Result<MediaMetadata>`.
4. **State Update**: `ViewModel` updates `StateFlow`. `MainScreen` recomposes to display the thumbnail and quality cards.
5. **Download Trigger**: User selects a quality and clicks Download. `MainScreen` calls `MainViewModel.download(stream)`.
6. **Background Task**: `ViewModel` delegates to a `ForegroundService` or `WorkManager` to prevent process death.
7. **Processing**: Download Engine fetches chunks. Once complete, Media Processing Layer merges streams.
8. **Storage**: Final file is moved to public storage via Platform Services.
9. **Feedback**: Progress and completion states flow back to the UI via broadcast receivers, callbacks, or shared flows.
