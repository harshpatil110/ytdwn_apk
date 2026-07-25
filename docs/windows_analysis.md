# Windows-Specific Code Analysis

This document identifies code implementations heavily tied to the Windows OS or generic desktop environments that will require strategic replacement during the Android migration.

## 1. Subprocess Window Hiding
- **File**: `core/ffmpeg_utils.py`
- **Function**: `merge_video_audio`, `convert_to_mp3`
- **Code snippet**:
  ```python
  if os.name == 'nt':
      startupinfo = subprocess.STARTUPINFO()
      startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
  ```
- **Reason**: Prevents the Windows command prompt from flashing on screen when FFmpeg is called.
- **Android Strategy**: Remove entirely. Subprocesses are not used this way on Android. Media processing will be handled by native APIs or JNI libraries (FFmpegKit), which do not spawn visible terminal windows.

## 2. Default Download Path
- **File**: `core/helpers.py`
- **Function**: `get_default_download_path`
- **Code snippet**:
  ```python
  base_path = os.path.join(os.path.expanduser("~"), "Downloads", "YouTube")
  ```
- **Reason**: Uses the OS user's home directory to locate the standard Desktop `Downloads` folder.
- **Android Strategy**: Android has a strict Sandboxed File System (Scoped Storage). `os.path.expanduser("~")` is invalid.
  - **Replacement**: Use `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)` via Android Context, or rely on `MediaStore` APIs to save media files directly to the device's public media collections.

## 3. Filename Sanitization
- **File**: `core/helpers.py`
- **Function**: `safe_filename`
- **Code snippet**:
  ```python
  return "".join(c for c in name if c not in r'\/:*?"<>|')
  ```
- **Reason**: Removes characters illegal in Windows NTFS file systems.
- **Android Strategy**: Keep. Android's underlying file systems (ext4, F2FS, FAT32 for SD cards) also have character restrictions. This logic remains valid and necessary.

## 4. Resource Path Resolution (PyInstaller)
- **File**: `core/helpers.py`
- **Function**: `resource_path`
- **Code snippet**:
  ```python
  base_path = sys._MEIPASS
  ```
- **Reason**: Allows the app to find bundled assets (like `icon.ico`) when compiled into a `.exe` using PyInstaller.
- **Android Strategy**: Remove. Android handles assets entirely differently using the `res/` and `assets/` folders within the APK, accessed via `Context.getResources()` or `Context.getAssets()`.

## 5. Executable Path Resolution
- **File**: `core/ffmpeg_utils.py`
- **Function**: `get_ffmpeg_path`
- **Code snippet**:
  ```python
  return shutil.which("ffmpeg")
  ```
- **Reason**: Searches the Windows system `PATH` for the `ffmpeg.exe` binary.
- **Android Strategy**: Remove. There is no global system `PATH` containing `ffmpeg` on standard unrooted Android devices. The FFmpeg library must be bundled inside the APK.

## 6. File Logging
- **File**: `core/helpers.py`
- **Function**: `setup_logging`
- **Code snippet**:
  ```python
  log_dir = os.path.join(get_default_download_path(), "logs")
  fh = logging.FileHandler(os.path.join(log_dir, "ytdwn.log"))
  ```
- **Reason**: Writes standard Python logs to a local file in the Downloads directory.
- **Android Strategy**: Remove file handler. On Android, logs should be directed to `Logcat` using `android.util.Log`. If file logging is strictly required for user debugging, it must be written to the app's internal cache dir (`Context.getCacheDir()`) to respect storage permissions.
