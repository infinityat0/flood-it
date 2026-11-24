# Flood It

A flood fill colour puzzle in the browser. Starting from the top-left
corner, pick colours to grow your flood until it covers the whole board.
Every board's move limit comes from a greedy solver, so it can always be
won within the limit.

## Play locally

No build step. Serve `public/` over HTTP (ES modules don't load from `file://`):

```sh
npm start          # or: python3 -m http.server -d public
```

## Test

```sh
npm test           # Node 18+, built-in test runner
```

## Layout

- `public/engine.js` – pure rules (board generation, flood fill, greedy move limit, win/loss)
- `public/main.js` – UI: board, colour buttons, keys 1–6, retry
- `public/index.html`, `public/base.css`, `public/style.css` – page and styling (light and dark)
- `test/` – engine tests

## Deploy

Static site; `vercel.json` serves `public/` with no build. Import the
repository in Vercel, or run `vercel --prod`.
