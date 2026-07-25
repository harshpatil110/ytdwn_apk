# Migration Strategy

This document outlines the step-by-step strategy for migrating the YTDWN desktop application to a production-ready Android application.

## Phase 1: Project Setup & Technical Foundation
1. **Initialize Android Studio Project**: Create a new Kotlin/Jetpack Compose project.
2. **Define Architecture**: Scaffold the directory structure (UI, Domain, Data, Platform).
3. **Evaluate Extraction Technology**: 
   - Decide between using **Chaquopy** to bridge the existing `pytubefix` Python code, OR replacing it entirely with a native Kotlin solution (`NewPipeExtractor`). 
   - *Recommendation*: Start with Chaquopy for rapid parity, but plan for a native rewrite for long-term performance and app size reduction.

## Phase 2: Core Engine Migration
1. **Implement Extraction**: Integrate the chosen extraction logic. Test URL parsing and stream discovery via Unit Tests before connecting to any UI.
2. **Implement FFmpeg**: Integrate `FFmpegKit` into the project. Replicate the `merge_video_audio` and `convert_to_mp3` logic using the library's asynchronous session execution.
3. **Implement Storage**: Create the `StorageRepository`. Implement `MediaStore` logic to handle saving `.mp4` and `.mp3` files to the public `Movies` and `Music` directories on Android 10+.

## Phase 3: Background Services
1. **Foreground Service Setup**: Android aggressively kills apps in the background. Implement an Android `Service` (Foreground) that displays a persistent notification with a progress bar.
2. **Connect Download Logic**: Bind the extraction and download loop to this service to ensure large files complete successfully if the user minimizes the app.

## Phase 4: UI Implementation
1. **Design System**: Translate Stitch Design Tokens (Colors, Fonts) into a Jetpack Compose `MaterialTheme`.
2. **Build Components**: Recreate `FlatButton`, `FlatEntry`, and `StitchQualityCard` as Compose `@Composable` functions.
3. **Build Screens**: Assemble the `MainScreen`. Implement a responsive layout (Scrollable column) that mimics the desktop canvas behavior but is optimized for mobile touch targets.
4. **Image Loading**: Integrate `Coil` for asynchronous thumbnail fetching and caching.

## Phase 5: Integration & State Management
1. **ViewModels**: Implement the presentation logic. Map raw data structures to Kotlin Data Classes.
2. **Data Binding**: Connect the Compose UI to the ViewModel `StateFlows`.
3. **Error Handling**: Implement `Snackbars` or Dialogs for user feedback on network failures or parsing errors.

## Phase 6: Testing & QA
1. **Permissions Testing**: Verify behavior on Android 11+ (Scoped Storage) and Android 9- (Legacy Storage).
2. **Lifecycle Testing**: Ensure downloads survive screen rotations, app minimization, and device sleep states.
3. **Format Testing**: Verify adaptive merges (4K video + audio) and audio extraction (MP3) play correctly on the device's default media players.

## Phase 7: Optimization & Release
1. **APK Size Reduction**: If using Chaquopy and FFmpegKit, the APK size will be substantial. Configure ABI splits to generate separate APKs for `arm64-v8a` and `armeabi-v7a`.
2. **ProGuard**: Configure obfuscation and shrinking rules.
3. **Final Review**: Validate against all features listed in the `feature_inventory.md`.
