# Flood It

A flood fill colour puzzle, as a web game and a native Android app. Starting
from the top-left corner, pick colours to grow your flood until it covers the
whole board. Every board's move limit comes from a greedy solver, so it can
always be won within the limit.

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

A native Kotlin + Jetpack Compose port with the same rules, board sizes, move
limits and colours. Build and install on a connected device or emulator:

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
  - `app/src/main/java/com/infinityat0/floodit/ui/` – ViewModel, theme and Compose screen
  - `app/src/test/` – engine tests and a Robolectric UI test

## Deploy

Static site; `vercel.json` serves `web/public/` with no build. Import the
repository in Vercel, or run `vercel --prod`.
