# Backend Separation Report

This report analyzes the existing backend codebase to distinguish between reusable business logic, platform-specific logic, and UI-coupled logic. This separation is critical for the Android migration.

## 1. Reusable Business Logic

The following components are pure Python and do not depend on the OS or UI. If using a Python-to-Android bridge (like Chaquopy), these can remain largely unchanged.

- **Stream Fetching**: The `YouTube` object initialization and metadata extraction (`yt.title`, `yt.length`, `yt.views`).
- **Stream Discovery**: The filtering and sorting logic in `fetch_streams` (e.g., `yt.streams.filter(type="video")`, `order_by("resolution")`).
- **Data Structuring**: The `unique_videos` and `unique_audios` lists generation, preventing duplicate qualities.
- **Formatting Utilities**: 
  - `format_size(bytes_size)`: Converts bytes to KB/MB/GB.
  - `safe_format_name(mime_type)`: Parses mime types into standard strings.
  - `extract_res_val`, `extract_abr_val`: Extracts numerical values from strings for sorting.
  - `safe_filename(name)`: Sanitizes strings to prevent file system errors.

## 2. Platform Logic (Needs Refactoring)

These components rely heavily on desktop OS features (Windows/Linux/macOS) and will crash or misbehave on Android.

- **FFmpeg Execution (`core/ffmpeg_utils.py`)**:
  - `subprocess.run()` is used to invoke the `ffmpeg` binary.
  - OS-specific window flags (`subprocess.STARTUPINFO()`).
  - **Android Need**: Must be replaced with an Android native library like `FFmpegKit`.
- **Path Resolution (`core/helpers.py`)**:
  - `get_default_download_path()` uses `os.path.expanduser("~")`.
  - `resource_path()` uses `sys._MEIPASS`.
  - **Android Need**: Must use Android Context to resolve internal paths (`Context.getFilesDir()`) or public paths (`MediaStore`).
- **Logging Configuration (`core/helpers.py`)**:
  - `setup_logging()` creates a physical `.log` file in the user's download directory.
  - **Android Need**: Route logs to Android `Logcat` instead of the local filesystem due to permission constraints.

## 3. UI-Coupled Logic (Needs Refactoring)

These components are technically in the backend but are structured specifically for the Tkinter frontend.

- **Callbacks**: The `status_callback` pattern passed into `fetch_streams` and `download_video`. It expects specific string identifiers (`"info"`, `"error"`, `"streams_fetched"`).
  - **Android Need**: While callbacks work in Kotlin, a reactive stream (like Kotlin `Flow` or `LiveData`) is preferred. The payload structures should be mapped to strongly-typed Data Classes (Models) rather than raw dictionaries.
- **Progress Hook**: The `_progress_hook` calculates percentages and formats string messages (e.g., `"Downloading... 45%"`).
  - **Android Need**: The backend should emit raw progress data (bytes downloaded, total bytes) and let the Android Presentation Layer handle string formatting and UI updates.
- **Thread Management**: Currently, the UI (`app_ui.py`) spawns raw `threading.Thread` objects.
  - **Android Need**: Threading should be managed by the Application Layer (ViewModels) using Kotlin Coroutines, removing the need for manual threading considerations in the Python backend.
