# Risk Assessment

This document outlines the major technical risks associated with migrating the YTDWN desktop application to Android, their potential impact, and mitigation strategies.

## 1. Media Processing (FFmpeg Integration)
- **Risk**: Standard Python `subprocess` calls to a bundled FFmpeg executable will not work on Android. Android requires specifically compiled native libraries (JNI/C++) to run FFmpeg.
- **Severity**: Critical. The app cannot merge high-quality video or convert audio without it.
- **Likelihood**: Certain.
- **Mitigation**: 
  - Do not attempt to bundle a raw executable.
  - Integrate a dedicated Android library like `FFmpegKit`. 
  - Ensure the correct GPL/LGPL licenses are respected.
  - *Alternative*: Rewrite the merge logic using Android's native `MediaMuxer` API (highly complex, but reduces APK size).

## 2. Storage Permissions (Scoped Storage)
- **Risk**: Android 10 (API 29) introduced Scoped Storage, restricting apps from freely writing to the filesystem using standard file paths (e.g., `os.path.join("/sdcard/Downloads")`).
- **Severity**: Critical. Failure to comply results in `PermissionDenied` crashes.
- **Likelihood**: Certain.
- **Mitigation**:
  - Abandon raw file path I/O for final output.
  - Implement Android's `MediaStore` API to insert files into public media collections (Downloads, Movies, Music).
  - Use App-Specific directories (`Context.getExternalFilesDir()`) for temporary files during the FFmpeg merge process.

## 3. Background Execution (Process Death)
- **Risk**: Desktop apps can run downloads in background threads indefinitely. Android OS aggressively kills background apps to save battery (Doze mode, App Standby).
- **Severity**: High. Large 4K downloads will fail if the user locks their screen or switches apps.
- **Likelihood**: High.
- **Mitigation**:
  - Use a `Foreground Service` paired with an ongoing Notification for the duration of the download. 
  - This signals to the OS that the app is actively performing a user-requested task and should not be killed.

## 4. UI Framework Incompatibility (Tkinter)
- **Risk**: Tkinter is completely unsupported on Android.
- **Severity**: Critical.
- **Likelihood**: Certain.
- **Mitigation**: 
  - The UI must be rewritten entirely. Use Jetpack Compose for the fastest path to a modern, declarative UI that maps well to the existing component architecture.

## 5. Python Runtime on Android (Chaquopy)
- **Risk**: If retaining the Python backend via Chaquopy, there is a performance overhead, increased APK size, and potential compatibility issues with specific C-based Python modules.
- **Severity**: Medium.
- **Likelihood**: Medium.
- **Mitigation**:
  - `pytubefix` is pure Python, minimizing compatibility risks.
  - Profile memory usage during downloads.
  - If performance is unacceptable, the extraction logic must be rewritten in Kotlin/Java.

## 6. Network Security Configuration
- **Risk**: Android 9 (API 28) disables Cleartext HTTP traffic by default. 
- **Severity**: Low.
- **Likelihood**: Low (YouTube uses HTTPS).
- **Mitigation**: Ensure all network calls in the extraction layer explicitly use `https://`.

## 7. Configuration Changes (Lifecycle)
- **Risk**: Rotating the device on Android destroys and recreates the `Activity`. This can cause UI state loss or trigger duplicate downloads if not handled correctly.
- **Severity**: Medium.
- **Likelihood**: High.
- **Mitigation**: 
  - Store UI state inside a `ViewModel` which survives configuration changes.
  - Ensure the download service runs independently of the Activity lifecycle.
