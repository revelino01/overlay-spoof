# Ludashi Benchmark Spoof

An ultra-lightweight Android application designed to bypass and trick OEM benchmark detection systems (such as those found on Xiaomi, vivo/iQOO, OPPO, Realme, etc.). 

By masquerading as the `com.ludashi.benchmark` package and maintaining a persistent foreground state, this app forces your device into its maximum performance/gaming power profile while you freely play other games or use other apps.

---

## How It Works

Chinese OEMs often boost CPU/GPU frequencies and loosen thermal limits when they detect a known benchmarking app is running in the foreground. 

This app tricks `ActivityManager` into thinking the benchmark is always active:

1. **Picture-in-Picture (PiP) Foreground State**:
   - Android OEMs check the *foreground activity* to trigger their performance boost. A background service or an overlay window is not enough.
   - When launched, this app instantly enters **Picture-in-Picture (PiP) mode**, rendering itself as a completely transparent 1x1 square.
   - PiP is a special Android state that keeps the activity registered as a **FOREGROUND component** even when you navigate away to play a game. The OEM detector sees the spoof as actively running on top.

2. **Persistent Foreground Service**:
   - A lightweight background service (`FOREGROUND_SERVICE_SPECIAL_USE`) runs simultaneously. This ensures the Android memory killer doesn't terminate the spoof process when your device is under heavy load (like when playing a demanding game).

3. **Zero Resource Overhead**:
   - The app has no UI, no rendering loops, and no timers. Idle CPU usage is **0.0%**. It just acts as a transparent dummy placeholder to trigger the system's power profile.

---

## Usage & The "Stash" Trick

1. **Start the Spoof**: Tap the app icon. A toast will say "Benchmark spoof active" and a tiny invisible PiP square will appear on your screen.
2. **Stash the PiP Window (Important!)**: 
   - Because the PiP window sits on top, it might block your touches in that small area.
   - On Android 12+, **swipe the invisible square all the way off the left or right edge of your screen**. 
   - It will "stash" itself into a tiny, barely visible handle on the edge of your screen and **stop blocking your touches entirely**. The spoof remains 100% active!
3. **Play Your Game**: Open any heavy game. The OEM will apply the benchmark power profile.
4. **Stop the Spoof**: Tap the ongoing notification in your notification tray or tap the app icon again to completely kill the spoof.

---

## Project Structure

```
├── .github/
│   └── workflows/
│       └── build.yml               # GitHub Actions CI workflow (builds APKs on push)
├── app/
│   ├── build.gradle.kts            # App module build script (Target SDK 34)
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml # Declares PiP support, Foreground Services
│           ├── java/com/ludashi/benchmark/
│           │   ├── OverlayActivity.kt # PiP Activity logic
│           │   ├── OverlayService.kt  # Persistent Foreground Service
│           │   └── OverlayReceiver.kt # Broadcast receiver to stop the spoof
│           └── res/
│               ├── drawable/       # Launcher vector assets
│               ├── mipmap-anydpi-v26/ # Adaptive icon definitions
│               └── values/         # strings.xml, colors.xml, themes.xml
├── build.gradle.kts                # Root project build configuration
└── settings.gradle.kts             # Module and repository definitions
```

---

## Installation via GitHub Actions

This repository automatically builds the APK via GitHub Actions every time code is pushed.

1. Go to the **Actions** tab in this repository.
2. Click on the latest successful **"Build Android APK"** workflow run.
3. Download the generated APK from the **Artifacts** section at the bottom.
4. Install `app-debug.apk` or `app-release.apk` on your device.

```bash
adb install app-release.apk
```
