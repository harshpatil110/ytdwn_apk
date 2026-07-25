# YTDWN Android - Release & Build Guide

## Overview
This document outlines the standard procedure for building, signing, and releasing the **YTDWN Android Application**. The repository is fully configured for production.

## 1. Prerequisites
- **Android Studio** (Koala / Ladybug or newer recommended)
- **Java Development Kit (JDK) 17** (Embedded in Android Studio)
- **Android SDK** (API 34)

## 2. Versioning Configuration
Versioning is completely decoupled from the Gradle build files to prevent accidental commits of hardcoded values.
To bump the application version before a release, edit `android/gradle.properties`:
```properties
VERSION_CODE=2
VERSION_NAME=1.0.1
```
The build system will automatically inject these values into the generated APKs.

## 3. Signing Configuration
The project is configured to automatically search for release keystore properties. You do **not** need to hardcode passwords in the repository.

When building via CI/CD or CLI, you can inject the signing properties:
```bash
./gradlew assembleRelease \
  -PRELEASE_STORE_FILE=path/to/keystore.jks \
  -PRELEASE_STORE_PASSWORD=your_password \
  -PRELEASE_KEY_ALIAS=your_alias \
  -PRELEASE_KEY_PASSWORD=your_key_password
```
*Note: If no release signing properties are provided, the `assembleRelease` task will automatically fall back to the `debug` signing config to allow testing of minified builds.*

## 4. Build Optimization & Shrinking
The `release` build type is heavily optimized:
*   `isMinifyEnabled = true`: Strips unused Kotlin/Java code via R8.
*   `isShrinkResources = true`: Removes unused XML layouts and drawables, significantly reducing the final APK size.
*   **ABI Filters**: The NDK is strictly filtered to `arm64-v8a`, `armeabi-v7a`, and `x86_64` to prevent massive Chaquopy bloat.

## 5. Generating APKs Locally

### Option A: Via Android Studio (Recommended)
1. Open the `android` folder in Android Studio.
2. Go to **Build > Generate Signed Bundle / APK**.
3. Select **APK**.
4. Provide your Keystore credentials (or create a new one).
5. Select the **release** variant.
6. Click **Finish**. The APK will be generated in `android/app/release/`.

### Option B: Via Command Line
1. Open a terminal in the `android` directory.
2. Generate Debug APK (for testing):
   ```bash
   ./gradlew assembleDebug
   ```
   *Output: `android/app/build/outputs/apk/debug/app-debug.apk`*
3. Generate Release APK (unsigned or debug-signed fallback):
   ```bash
   ./gradlew assembleRelease
   ```
   *Output: `android/app/build/outputs/apk/release/app-release.apk`*

## 6. Final QA & Installation Verification
Before distributing the Release APK, verify:
1. **Fresh Install**: Ensure the app installs without package conflict errors.
2. **Metadata Fetch**: Paste a YouTube URL and verify thumbnails load (validates Internet permissions and R8 proguard rules).
3. **Storage Access**: Test saving a file to a custom SAF location (validates Scoped Storage).
4. **Log Validation**: Check Logcat to ensure no sensitive URL signatures are exposed.

## 7. Known Limitations
- The application currently packages the Chaquopy Python runtime and FFmpegKit. The resulting universal APK may be large (~80MB). For Play Store distribution, building an **Android App Bundle (.aab)** via `bundleRelease` is highly recommended, as it will serve optimized splits to user devices (~30MB).
