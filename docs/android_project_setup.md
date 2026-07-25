# Android Project Setup & Initialization

## Overview
The `android/` directory contains the foundational, production-ready Android project for YTDWN. It has been initialized with Jetpack Compose, Kotlin DSL Gradle scripts, and a clean package architecture designed to support the eventual porting of the desktop downloader logic.

## Folder Explanation
- `app/src/main/java/com/ytdwn/app/`: Root package for Kotlin source code.
  - `presentation/`: Contains Jetpack Compose UI, Themes (Colors, Typography), Components, and Screens.
  - `domain/`: Reserved for pure business logic UseCases (Downloading, Metadata fetching).
  - `data/`: Reserved for implementations of Repositories (Networking, Storage, Media processing).
  - `utils/`: Contains centralized Application Configuration and Logging mechanisms.
- `app/src/main/res/`: Android Resources.
  - `mipmap-anydpi-v26/`: Adaptive launcher icons for modern Android versions.
  - `values/`: Colors, Strings, and Splash Themes.

## Application Configuration
Configuration is centralized in `com.ytdwn.app.utils.Configuration`. This avoids magic numbers scattered throughout the app, offering a single source of truth for timeout durations, temporary folder names, and future feature flags.

## Permissions Strategy
Permissions have been pre-declared in `AndroidManifest.xml`:
- `INTERNET` & `ACCESS_NETWORK_STATE`: For networking.
- `FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_DATA_SYNC`: To ensure background downloads aren't killed by the OS.
- `POST_NOTIFICATIONS`: Required to show progress bars in the notification tray (Android 13+).
*Note: Storage permissions (`WRITE_EXTERNAL_STORAGE`) are excluded by design, as the project will use modern Android `MediaStore` APIs.*

## Build Instructions & Development Setup
1. Open the `android/` directory directly in **Android Studio**.
2. Wait for Gradle Sync to complete. The project uses Android Gradle Plugin 8.2.0 and Kotlin 1.9.0.
3. Build the project using the "Make Project" hammer icon, or via command line:
   ```bash
   ./gradlew assembleDebug
   ```

## Run Instructions
1. Connect an Android device (API 24 or higher) or start an Emulator.
2. Click the Run button in Android Studio, or execute:
   ```bash
   ./gradlew installDebug
   ```
3. The app will launch, showing the Splash theme briefly before transitioning to the Jetpack Compose `MainActivity` foundation.

## Future Module Roadmap
The next tasks will involve filling in the `domain` and `data` packages:
1. **Extraction Engine**: Creating the Python bridge (Chaquopy) or native equivalent in `data/networking`.
2. **Media Processing**: Integrating FFmpegKit into `data/media`.
3. **Storage Engine**: Implementing the MediaStore logic in `data/storage`.
4. **UI Refinement**: Building out `presentation/screens` to mirror the desktop UI workflow.
