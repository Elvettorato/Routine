# Changelog

## [1.3] - 2026-06-26

### Added
- Automatic crash log capture via custom `UncaughtExceptionHandler`
- Notification with crash details when app crashes
- `CrashActivity`: scrollable crash log viewer with share button
- "Test crash handler" button in Settings to verify the system
- Crash report strings in all 28 languages (English fallback)
- `buildConfig = true` in build.gradle.kts for `BuildConfig` generation

### Fixed
- French/Catalan `strings.xml` converted from UTF-16LE to UTF-8 (was causing aapt2 build errors)
- Unescaped apostrophes in string resources now properly escaped with `\'`

## [1.2] - 2026-06-25

### Added
- Custom DND interruptions: allow calls/messages/alarms/media/system from contacts/starred/everyone/nobody
- Quick Settings Tile showing active routine count
- "Run Now" button on routine cards in HomeScreen
- Settings screen with location polling interval, background permission status, version, credits
- Settings icon in top bar
- GitHub link card in Settings
- Automatic update check from GitHub releases (`UpdateChecker`)
- "Update available" card in Settings (tertiaryContainer style)
- `KeepAliveReceiver` with 30-min keep-alive alarm to prevent app from being killed
- Battery optimization exemption card in Settings (`ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`)
- Light theme support with system theme detection (`isSystemInDarkTheme()`)
- Dynamic light/dark color schemes on API 31+, LineageOS palettes for pre-31

### Changed
- Ringer Mode action: Normal / Vibrate / Silent
- Time picker: replaced +/- buttons with Material 3 clock-face `TimePicker`
- Location monitoring: rewritten with `getCurrentLocation()` polling + enter/exit state tracking
- `START_STICKY` → `START_REDELIVER_INTENT` for service reliability
- `Get Current Location` in EditorScreen uses `getCurrentLocation()` instead of `lastLocation`
- All hardcoded colors replaced with `MaterialTheme.colorScheme.*`
- `cancelTimeAlarms()` now cancels all 7 per-day PendingIntents instead of only current `triggerDaysOfWeek`
- EditorViewModel no longer hardcodes `isEnabled = true` — preserves existing state from DB
- Added `isEnabled` guard in `RoutineScheduler.schedule()`

### Fixed
- Geofence PendingIntent request code conflict: changed from `0` to `routine.id`
- PendingIntent mutability: `FLAG_MUTABLE` on API 31+ for GeofenceHelper and LocationUpdateReceiver
- `foregroundServiceType="dataSync"` → `"location|dataSync"` for Android 14+ geofencing
- `MissingPermission` lint suppressed in ActionExecutor/EditorScreen/GeofenceHelper
- `startActivityAndCollapse(Intent)` deprecated → PendingIntent variant on API 34+ with fallback
- `ArrowBack` → `AutoMirrored.Filled.ArrowBack`, `VolumeUp` → `AutoMirrored.Filled.VolumeUp`
- `mutableStateOf<Int>` → `mutableIntStateOf` (6 places)
- French/Catalan strings.xml converted from UTF-16LE to UTF-8 (was causing aapt2 errors)
- Unescaped apostrophes in string resources now properly escaped with `\'`

## [1.1] - 2026-06-25

### Added
- Ringer Mode action with Normal / Vibrate / Silent
- Custom DND customization (allow calls, messages, alarms, media, system from contacts/starred/everyone/nobody)
- Quick Settings Tile (`RoutineTileService`)
- Settings screen with location interval selector and credits
- `SettingsManager` (SharedPreferences) for configurable location polling interval
- 28 language translations for all strings

### Changed
- Time picker: Material 3 clock face
- Replaced `+-` buttons with Material 3 TimePicker

## [1.0] - 2026-06-25

### Added
- Initial release
- Time and location triggers
- Volume, brightness, DND, notification, ringer actions
- Geofencing via Google Play Services
- Material 3 dark theme
- 28 language support
