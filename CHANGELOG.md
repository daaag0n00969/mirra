# Changelog

## 0.1.2 — 2026-09-19

### Fixed
- Crash on start: `TextureView doesn't support displaying a background drawable`. Overlay failed to appear even with all permissions granted.

## 0.1.1 — 2026-09-19

### Fixed
- Overlay no longer blends with the unflipped app underneath (the “double image” / ghosting). Window stays fully opaque; brightness fades to black, not to the original player.

### Changed
- Accessibility is recommended so tap-through still works on a fully opaque overlay (Android 12+ untrusted-touch rules).

## 0.1.0 — 2026-09-19

First public sideload build. Not listed on Google Play, RuStore, or F-Droid yet.

### Added
- Single-app screen capture overlay with horizontal flip, vertical flip, and 180° rotation
- **Pass-through taps** so player / maps controls keep working under the mirrored picture
- **Floating bubble** over other apps: tap to show/hide the overlay, drag to move, long-press for flip / stop
- Optional **mirrored taps** via Accessibility (tap where a control appears after the flip)
- Notification actions: hide/show overlay, stop session
- Overlay opacity, start delay, keep-screen-on, crop system bars
- English and Russian UI
- Dark HUD-styled home, settings, and help screens
