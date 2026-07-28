# YTDWN - Premium YouTube Downloader

![Version](https://img.shields.io/badge/version-2.0.0-beige.svg) ![Platform](https://img.shields.io/badge/platform-android%20%7C%20windows-blue.svg) ![License](https://img.shields.io/badge/license-MIT-green.svg)

**YTDWN** is a premium YouTube downloader available for both **Windows Desktop** and **Android**, designed to deliver a clean, modern downloading experience with complete control over video and audio quality. Users can preview available streams, select their preferred resolution or bitrate, and download content directly to their device with a fast, intuitive interface.

## ✨ Features

- **🎬 Download Any Available Quality** – View every available video resolution before downloading, including 144p, 240p, 360p, 480p, 720p, 1080p, and higher whenever available.
- **🎵 Multiple Audio Qualities** – Browse and download available audio streams with different bitrates and formats before starting the download.
- **📋 Stream Information** – Displays video thumbnail, title, channel, duration, upload date, views, codec, FPS, format, and estimated file size.
- **⚡ Quality Selection Interface** – Choose exactly which video and audio stream to download through an intuitive selection interface.
- **🛠 Automatic FFmpeg Processing** – Automatically merges adaptive video and audio streams into a single MP4 file and supports high-quality MP3 conversion.
- **📂 Custom Download Location** – Save downloads to a user-selected directory with automatic filename sanitization and duplicate handling.
- **📊 Live Download Progress** – Real-time progress bar with download percentage, transfer speed, ETA, and status updates.
- **🖥 Premium Desktop Experience** – Modern Windows application built with a clean professional interface.
- **📱 Native Android Version** – Android application with the same workflow, quality selection, and download experience as the desktop version.
- **🚀 Responsive Architecture** – Background downloading and processing ensure the interface remains smooth during large downloads.

## 📥 Installation

### Desktop (Windows)

### Prerequisites

- Python 3.10+
- FFmpeg (Added to System PATH)

### Setup

1. Clone the repository:

```bash
git clone https://github.com/yourusername/ytdwn.git
cd ytdwn
```

2. Install dependencies:

```bash
pip install -r requirements.txt
```

3. Launch the application:

```bash
python main.py
```

---

### Android

Build the Android project using Android Studio.

Requirements:

- Android Studio Hedgehog or newer
- Android SDK 34+
- Gradle
- Emulator or Physical Android Device

Run directly from Android Studio or generate an APK for installation.

---

## 🚀 Usage

### Desktop

1. Launch YTDWN.
2. Paste a valid YouTube URL.
3. Press **ENTER**.
4. The application fetches:
   - Thumbnail
   - Title
   - Channel
   - Duration
   - Upload Date
   - View Count
   - Available Video Qualities
   - Available Audio Qualities
5. Select your preferred video quality.
6. Select your preferred audio quality.
7. Choose the download destination (optional).
8. Click **Download**.
9. The application downloads, merges, and saves the final media automatically.

---

### Android

1. Open the application.
2. Paste a YouTube URL.
3. Tap **ENTER**.
4. Review all available video and audio streams.
5. Select your preferred quality.
6. Tap **Download**.
7. The file is downloaded, processed, and saved directly to the device's Downloads folder (and made available through the system media library where applicable).

---

## 📦 Build for Production

### Windows EXE

```bash
pip install pyinstaller

pyinstaller --noconfirm ^
--onefile ^
--windowed ^
--icon assets/icon.ico ^
--name YTDWN ^
main.py
```

---

### Android APK

Using Android Studio:

```
Build
    → Build Bundle(s) / APK(s)
        → Build APK(s)
```

Release APK:

```
Build
    → Generate Signed Bundle / APK
```

The generated APK can then be installed directly on any supported Android device.

---

## 🏗 Tech Stack

### Desktop

- **Frontend:** Python (Tkinter)
- **Backend:** Python
- **Downloader:** Pytubefix
- **Media Processing:** FFmpeg
- **Packaging:** PyInstaller

### Android

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Downloader:** Embedded Python (Chaquopy + Pytubefix)
- **Media Processing:** FFmpegKit
- **Architecture:** MVVM
- **Build System:** Gradle
- **Packaging:** Android APK / AAB

---

## 📁 Supported Downloads

### Video

- MP4
- Highest Available Resolution
- User Selectable Resolution
- Adaptive Stream Merging

### Audio

- MP3
- M4A
- WEBM
- Multiple Bitrates
- User Selectable Quality

---

## 🔒 Highlights

- Automatic YouTube metadata extraction
- Quality preview before downloading
- Adaptive video/audio stream merging
- MP3 conversion support
- Background downloading
- Automatic temporary file cleanup
- Download progress monitoring
- Custom save location
- Cross-platform support (Windows & Android)
- Modern, minimal user experience

---

*Designed for performance, flexibility, and a premium downloading experience across desktop and Android.*