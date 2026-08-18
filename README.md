# AEROMute 🚀

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg?style=flat-svg&logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-7F52FF.svg?style=flat-svg&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack--Compose-1.5.0-4285F4.svg?style=flat-svg&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Download APK](https://img.shields.io/badge/Download-APK_Latest-blue.svg?style=for-the-badge&logo=android)](https://github.com/Reefoldfiveteen/AEROMute/raw/main/release/AEROMute.apk)

**AEROMute** is a modern, premium, and highly customizable floating volume control and instant mute utility for Android. Built with Jetpack Compose and modern Material 3 design guidelines, it provides a seamless overlay widget that allows you to monitor and manage all device audio streams instantly without leaving your current app, game, or video.

This project is inspired by combining and merging two awesome open-source repositories:
*   [FloatingMute](https://github.com/mkalmousli/FloatingMute) by [@mkalmousli](https://github.com/mkalmousli)
*   [FloatingVolume](https://github.com/mkalmousli/FloatingVolume) by [@mkalmousli](https://github.com/mkalmousli)

---

## ✨ Features

*   **📺 Floating Widget Overlay**: An interactive on-screen bubble/badge that overlays any app.
*   **🖐️ Smart Gestures**:
    *   **Drag & Position**: Free drag-and-drop movement across the screen with optional edge-snapping.
    *   **Single Tap**: Expands the detailed volume slider panel.
    *   **Double Tap**: Instant mute/unmute action for all active streams.
*   **🎛️ Dual Widget Modes**:
    *   **Standard Mode**: Detailed multi-stream control panel with individual volume sliders.
    *   **Simple Mode**: A compact, minimal vertical bar containing only the mute state and current media volume percentage.
*   **🎚️ Instant Audio Profiles & Presets**: Apply optimized multi-stream volume levels in a single tap:
    *   **Gaming Mode**: High media volume, muted ringtone/notifications.
    *   **Silent / Meeting**: Completely muted media, ring, notification, and call streams.
    *   **Outdoor / Loud**: Max volume across all audio streams.
    *   **Night / Sleep**: Low volume for alarm and phone calls with zero media distraction.
    *   **Media Focus**: Balanced media playback with muted notifications.
*   **⏱️ Timed Quick Mute (Countdown)**: Mute device temporarily with an automated countdown timer (15m, 30m, 1h, 2h) that restores sound levels when the timer expires.
*   **🛡️ Safety Guard & Disconnect Protection**: Automatically mutes Media audio as soon as Bluetooth or wired headphones disconnect to prevent embarrassing loud speaker blaring in public.
*   **📻 Live Audio Output Monitor**: Real-time detection and display of active sound destinations (Built-in Device Speaker, Wired Headphones, or Bluetooth Wireless Audio).
*   **🔊 Audio Stream Management**: Control individual streams: Media (Music/Videos/Games), Ringtone, Notification, Alarm, and Phone Calls.
*   **🎨 Premium Customizations**:
    *   **Theme Presets**: 5 stylish built-in palettes: Indigo Aero, Cyber Cyan, Neon Emerald, Sunset Rose, and AMOLED Minimal.
    *   **Custom Shapes & Badge Form**: Personalisasi bentuk floating badge: Circle (Lingkaran), Squircle (Persegi Melengkung), Pill Badge (Kapsul), and Teardrop (Tetesan Air).
    *   **Transparency Adjustment**: Seamless opacity setting (from 15% to 100%) to match your taste.
    *   **Widget Scale Sizes**: Small (46dp), Medium (58dp), or Large (70dp) options.
    *   **Haptic / Vibration Feedback**: Tactile click response upon tapping and muting.
    *   **Auto-collapse Timer**: Auto-collapses back to a badge after a user-specified inactivity timeout (e.g., 5 seconds).
*   **⚡ Quick Settings Tile**: Quick Settings integration allows toggling the floating service or executing a "Mute All" command directly from your Android notification drawer.
*   **🔄 Real-time Synchronization**: Uses a custom broadcast receiver (`VolumeChangeReceiver`) to instantly detect hardware key presses or external volume changes and sync the overlay widget state.
*   **🌐 Localization**: Full support for both **English** and **Bahasa Indonesia**.
*   **🌙 Dark Mode Support**: Choose between System-matching, Force Dark, or Force Light modes.

---

## 🛠️ System Permissions Needed

AEROMute respects your privacy but requires two core system permissions to function correctly:

1.  **Display Over Other Apps (Overlay Permission)**: Allows the floating bubble and panel to appear on top of other active applications.
2.  **Do Not Disturb Access (Notification Policy Access)**: Required on modern Android versions (API 23+) to programmatically mute the Ringtone and Notification audio streams.

The app provides a direct permission onboarding card to guide you to the settings page.

---

## 📦 Getting Started

### 📲 Direct Installation (APK)
You can directly download and install the pre-compiled APK:
*   📥 **[Download AEROMute.apk (Direct Raw Download)](https://github.com/Reefoldfiveteen/AEROMute/raw/main/release/AEROMute.apk)**
*   📂 **[Browse APK in Release Folder (GitHub File Tree)](https://github.com/Reefoldfiveteen/AEROMute/tree/main/release/AEROMute.apk)**

### Prerequisites
*   Android device running Android 5.0 (API level 21) or higher.
*   [Android Studio](https://developer.android.com/studio) Jellyfish or newer.
*   Kotlin Gradle DSL.

### Building & Running from Source
1.  Clone the repository:
    ```bash
    git clone https://github.com/Reefoldfiveteen/AEROMute.git
    cd AEROMute
    ```
2.  Open the project in **Android Studio**.
3.  Let Gradle sync and download dependencies.
4.  Remove/modify the following line in the app's `build.gradle.kts` if you are using your own debug configurations:
    ```kotlin
    signingConfig = signingConfigs.getByName("debugConfig")
    ```
5.  Build and run the project on an emulator or physical device.
    > 💡 **Note**: The Gradle build script is configured with an automated task (`copyApkToRelease`) that automatically updates and copies the freshly compiled APK to `/release/AEROMute.apk` whenever you assemble the project.

---

## 📲 Quick Settings Tile Setup

To add the AEROMute tile to your notification panel:
1.  Swipe down from the top of your screen to open the notification drawer.
2.  Tap the **Pencil / Edit** icon.
3.  Scroll down to find the **AEROMute** tile.
4.  Drag it into your active Quick Settings section.
5.  Tap the tile to instantly toggle the floating widget service or toggle the master mute!

---

## 🤝 Contributing & Acknowledgments

This app is developed and maintained by [@Reefoldfiveteen](https://github.com/Reefoldfiveteen). Special thanks to [@mkalmousli](https://github.com/mkalmousli) for the original codebase inspirations of `FloatingMute` and `FloatingVolume`.

Contributions are welcome! Please feel free to open issues or submit pull requests.

---

## 📄 License

This project is licensed under the Apache License 2.0. See the `LICENSE` file for details.
