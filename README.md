# Spoof Transparent Overlay App

An ultra-lightweight Android application that runs as a **100% transparent active activity on top of the screen**, showing everything underneath clearly while consuming virtually zero CPU, GPU, or memory resources.

---

## Key Features

1. **Active Foreground Activity**:
   - Stays on top as the active foreground task recognized by Android's window manager and usage tracking.
2. **100% Optical Clarity**:
   - Completely transparent window (`@android:color/transparent`), no dimming (`backgroundDimEnabled = false`), and transparent status/navigation bars. Everything underneath is displayed clearly.
3. **Touch Passthrough**:
   - Uses `FLAG_NOT_TOUCHABLE` and `FLAG_NOT_TOUCH_MODAL` so all taps, swipes, and gestures pass straight through to whatever application is visible beneath the overlay.
4. **Zero Resource Overhead**:
   - No view rendering loops, no animations, no timers, and no background workers. Idle CPU usage is **0.0%**.
5. **Easy Dismissal**:
   - Because touch interactions pass through to underlying screens, an ongoing low-priority notification with a **Stop** action is provided in the notification drawer.
   - You can also dismiss the overlay by swiping it away from the Android **Recent Apps (Overview)** switcher or by re-tapping the app icon.

---

## Project Structure

```
├── .github/
│   └── workflows/
│       └── build.yml               # GitHub Actions CI workflow (builds APKs on push/dispatch)
├── app/
│   ├── build.gradle.kts            # App module build script (SDK 34, Java 17)
│   ├── proguard-rules.pro          # ProGuard rules for release builds
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # Declares transparent theme, launcher activity & receiver
│           ├── java/com/spoof/overlay/
│           │   ├── OverlayActivity.kt # Transparent activity logic & window flags
│           │   └── OverlayReceiver.kt # Broadcast receiver to dismiss overlay via notification
│           └── res/
│               ├── drawable/       # Launcher vector assets
│               ├── mipmap-anydpi-v26/ # Adaptive icon definitions
│               └── values/         # strings.xml, colors.xml, themes.xml
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties # Gradle 8.7 wrapper configuration
├── build.gradle.kts                # Root project build configuration
├── settings.gradle.kts             # Module and repository definitions
└── gradle.properties               # Memory and build parameters
```

---

## Building via GitHub Actions

This repository is configured with a GitHub Actions workflow in [`.github/workflows/build.yml`](file:///.github/workflows/build.yml):

1. **Push this repository** to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of Spoof Transparent Overlay"
   git branch -M main
   git remote add origin <your-github-repo-url>
   git push -u origin main
   ```
2. In GitHub, navigate to the **Actions** tab.
3. The **"Build Android APK"** workflow will trigger automatically (or click **"Run workflow"** manually).
4. Once completed, download the generated APK from the **Artifacts** section:
   - `spoof-overlay-debug` (debug APK ready for immediate sideloading)
   - `spoof-overlay-release` (optimized release APK)

---

## Installing on an Android Device

After downloading `app-debug.apk` or `app-release.apk` from GitHub Actions:

```bash
adb install app-debug.apk
```
Or transfer the APK file directly to your Android device and tap to install (enable "Install unknown apps" if prompted).
