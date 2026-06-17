# Routine

**Routine** is an Android app that lets you create automated routines triggered by time and/or location. When a trigger fires, the app executes a set of actions — DND mode, volume, brightness, ringer mode, or a notification — without any manual intervention.

Built with Jetpack Compose and Material 3, featuring a LineageOS-inspired dark theme with **dynamic color** support (Material You on Android 12+).

## Features

- **Time triggers** — set an exact time (with optional repeat on specific days of the week)
- **Location triggers** — define a geofenced area with configurable radius; trigger on enter, exit, or both
- **Background location polling** — periodic GPS/WiFi location checks as a fallback when geofencing is unreliable (configurable interval: 1–30 min)
- **Dual triggers** — time and location can be active on the same routine simultaneously
- **Actions**
  - Do Not Disturb (Off / Priority only / Total silence / Alarms only) **with customizable allowed interruptions** (calls/messages from nobody/contacts/starred/everyone, alarms, media, system)
  - Ringer Mode (Normal / Vibrate / Silent)
  - Volume (media, ring, alarm, notification — each 0–15)
  - Brightness (auto or manual level)
  - Custom notification with title and content
- **Run Now** — manually trigger any routine from the home screen
- **Quick Settings Tile** — shows active routine count, opens app on tap
- **Persistent storage** via Room database
- **Survives reboot** — re-schedules all enabled routines on boot
- **Settings** — configurable location check interval (1/2/5/10/15/30 min), background location permission status
- **Multi-language** — 28 languages
- **Dynamic colors** — automatically adapts to wallpaper color scheme (Android 12+)

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose (BOM 2024.12.01), Material 3, Material Icons Extended |
| Navigation | Navigation Compose 2.8.5 |
| Architecture | MVVM (ViewModel + StateFlow + Flow) |
| Database | Room 2.6.1 (with KSP) |
| Background work | Foreground Service (location type), AlarmManager, WorkManager 2.10.0 |
| Geofencing | Google Play Services Location 21.3.0 |
| Serialization | Gson 2.11.0 |
| Min / Target SDK | 26 / 35 |

## Screenshots

*(Add screenshots here)*

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 17
- A device or emulator running Android 8.0 (API 26) or higher

### Build

```bash
git clone https://github.com/elvettorato/routine.git
cd routine
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

### Permissions

The app requests the following permissions at runtime when needed:

| Permission | When |
|---|---|
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | Creating a location-based routine |
| `ACCESS_BACKGROUND_LOCATION` | Location-based routines to trigger in background (Android 10+) |
| `POST_NOTIFICATIONS` | Using notification actions (Android 13+) |
| `WRITE_SETTINGS` | Using brightness actions |
| `ACCESS_NOTIFICATION_POLICY` | Using DND actions |
| `SCHEDULE_EXACT_ALARM` | Using time triggers (Android 12+) |

## Architecture

```
MainActivity (single activity)
  └─ AppNavigation (NavHost)
       ├─ HomeScreen ─── HomeViewModel ─── RoutineForegroundService
       │                                      ├─ LocationRequest (periodic GPS polling)
       │                                      └─ GeofenceHelper (event-based)
       ├─ EditorScreen ── EditorViewModel
       └─ SettingsScreen
                              │
                    RoutineScheduler
                      ├─ AlarmManager (time)
                      ├─ GeofenceHelper (event-based location)
                      └─ ForegroundService (polling-based location)
                              │
                    ActionExecutor
                      ├─ NotificationManager (DND policy)
                      ├─ AudioManager (volume + ringer mode)
                      ├─ Settings.System (brightness)
                      └─ NotificationManagerCompat (notification)
                              │
                    RoutineRepository
                              │
                    RoutineDatabase (Room)
```

## License

```
MIT License

Copyright (c) 2026 Elvettorato

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
