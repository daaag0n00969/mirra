# Mirra

**Interactive HUD mirror for Android** — flip any app for a windscreen / HUD, without freezing the player underneath.

[![Release](https://img.shields.io/github/v/release/daaag0n00969/mirra?include_prereleases)](https://github.com/daaag0n00969/mirra/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-2ee6d6.svg)](LICENSE)
[![API](https://img.shields.io/badge/Android-14%2B-blue)](#requirements)

> Version **0.1.1** is a sideload preview. It is **not** published on Google Play, RuStore, or F-Droid yet.

[Русский README](README.ru.md) · [Releases / APK](https://github.com/daaag0n00969/mirra/releases)

---

## Why Mirra exists

Classic HUD-mirror apps draw a full-screen overlay of the captured app. That overlay **eats every tap**, so you cannot pause a video, skip a track, or tap a map while the mirror is on. You have to close the overlay, interact, then start over.

Mirra keeps the flipped picture and still lets you drive the app:

| Capability | What it does |
|---|---|
| **Pass-through overlay** | The mirrored layer is visual-only (`FLAG_NOT_TOUCHABLE`). Play / pause / seek hit the real player. The overlay is fully opaque so the original picture does not ghost through. |
| **Floating toggle** | A bubble sits over every window. Tap = hide or show the mirror instantly. Long-press = flip / stop. Drag to either edge. |
| **Mirrored taps** (optional) | With Accessibility enabled, taps on the flipped picture are remapped back onto the real controls. |
| **Does not die with the activity** | Swiping Mirra away does not kill an active session. Stop from the bubble or the notification. |

Independent original project (MIT). Inspired by the HUD-mirror idea; **not a fork** of Middor or any other AGPL app.

## Features

- Horizontal flip (windscreen), vertical flip, 180° rotation
- Single-app capture (Android 14 App Screen Sharing)
- Overlay opacity and start delay
- Keep screen on while the HUD is visible
- Optional crop of status / navigation bars
- Persistent notification with hide / show / stop
- English + Russian

## Requirements

- **Android 14 QPR2** or later (API 34+). Single-app capture is a platform feature.
- Overlay permission (`SYSTEM_ALERT_WINDOW`)
- Screen-capture consent (system sheet — pick **a single app**, never *Entire screen*)
- Notifications optional
- Accessibility optional, recommended for full-opacity pass-through on Android 12+ security rules and for remapped taps

## Install

1. Download `Mirra-0.1.1.apk` from [Releases](https://github.com/daaag0n00969/mirra/releases).
2. Allow install from your browser / Files.
3. Open Mirra → grant **Display over other apps**.
4. Tap **Start mirror** → choose **a single app** → open that app.

## Usage

1. Grant overlay (required) and, if you want, notifications + Accessibility.
2. **Start mirror**. In the capture sheet select **one application**.
3. Open the player, navigation, or whatever should sit on the HUD.
4. Place the phone facing the glass. Horizontal flip makes the reflection readable.
5. Use the **floating button**:
   - **Tap** — hide the overlay (full native controls) or show it again
   - **Drag** — park it on the left or right edge
   - **Long-press** — hide/show, flip horizontally, stop
6. Leave pass-through on unless you specifically need remapped taps.

### Touch modes

- **Pass-through (default)** — overlay does not consume MotionEvents. The player underneath stays fully functional.
- **Mirrored taps** — tap the control *where you see it* after the flip. Requires the Mirra Accessibility service. Gestures are inverted then injected.
- **Blocked** — turn both options off; only the bubble / notification can hide the overlay so you can tap.

## Permissions

| Permission | Why |
|---|---|
| `SYSTEM_ALERT_WINDOW` | Mirror overlay + floating button |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Capture while you use another app |
| `POST_NOTIFICATIONS` | Session controls in the shade |
| Accessibility (optional) | Trusted overlay window (`TYPE_ACCESSIBILITY_OVERLAY`) so full-opacity pass-through is allowed; remapped gestures |

Mirra does not read window text, does not log taps, and does not send data off-device.

## Build

```bash
# JDK 17+
./gradlew :app:assembleRelease
# APK → app/build/outputs/apk/release/app-release.apk
```

Release signing uses `app/release.keystore` (gitignored). If the keystore is missing, the build falls back to the Android debug key.

```bash
keytool -genkeypair -keystore app/release.keystore -alias mirra \
  -keyalg RSA -keysize 2048 -validity 10000
```

## Project layout

```
app/src/main/java/dev/dag0n/mirra/
  MainActivity.kt          UI host
  data/                    Settings
  mirror/                  Capture, overlay, bubble, notifications
  a11y/                    Optional Accessibility service
  ui/                      Compose screens
```

## Roadmap (after 0.1.x)

- Per-app profiles
- Black-level / night HUD filter
- Landscape crop presets
- Store listings (Play / RuStore / F-Droid) when 0.1.x is stable

## License

[MIT](LICENSE)

## Disclaimer

Use on the road at your own risk. Do not interact with the phone while driving. Mirra is a display helper, not a certified automotive HUD.
