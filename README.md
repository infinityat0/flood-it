# Flood It

A flood fill colour puzzle, as a web game and a native Android app. Starting
from the top-left corner, pick colours to grow your flood until it covers the
whole board. Every board's move limit comes from a greedy solver, so it can
always be won within the limit.

## Download for Android

**[Download Flood-It.apk](https://github.com/infinityat0/flood-it/releases/latest/download/Flood-It.apk)** (Android 8.0+).
Open it on your phone and allow installing from your browser or file manager when asked.
Every change to `android/` on `master` publishes a new build to
[Releases](https://github.com/infinityat0/flood-it/releases) via `.github/workflows/android-release.yml`.

## Play locally (web)

No build step. Serve `web/public/` over HTTP (ES modules don't load from `file://`):

```sh
cd web
npm start          # or: python3 -m http.server -d public
```

## Test (web)

```sh
cd web
npm test           # Node 18+, built-in test runner
```

## Android app

**Color Flood**: a native Kotlin + Jetpack Compose version with the same rules,
board sizes and move limits. The ☰ menu holds the settings:

- **Colors** – six color themes (Meadow, Ocean, Sunset, Pastel, Classic and a
  color-blind friendly Okabe–Ito set)
- **Appearance** – follow the system, or force light or dark
- **Board size** – 10×10, 14×14 or 18×18
- **How to play**

Settings are saved on the device and included in Android Auto Backup, so they
survive restarts and updates and come back after a reinstall when Google
backup is on.

Build and install on a connected device or emulator:

```sh
cd android
./gradlew installDebug
```

Or open `android/` in Android Studio. Run the unit and UI tests (JVM only,
Robolectric; screenshots land in `android/app/build/screenshots/`) with:

```sh
cd android
./gradlew testDebugUnitTest
```

## Layout

- `web/` – the browser game
  - `public/engine.js` – pure rules (board generation, flood fill, greedy move limit, win/loss)
  - `public/main.js` – UI: board, colour buttons, keys 1–6, retry
  - `public/index.html`, `public/base.css`, `public/style.css` – page and styling (light and dark)
  - `test/` – engine tests
- `android/` – the Android app
  - `app/src/main/java/com/infinityat0/floodit/game/` – pure Kotlin port of the engine
  - `app/src/main/java/com/infinityat0/floodit/ui/` – ViewModel and settings, color themes (`ColorThemes.kt`), Compose screen and menu
  - `app/src/test/` – engine tests and Robolectric UI tests (menu, themes, saved settings)

## Deploy

Static site; `vercel.json` serves `web/public/` with no build. Import the
repository in Vercel, or run `vercel --prod`.
