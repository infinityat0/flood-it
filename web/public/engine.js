// Pure rules for the flood fill puzzle. The board is a flat array of colour
// indices; the flood starts in the top-left corner. No DOM access here.

export const PALETTE = ["#541E32", "#8E3557", "#88A33E", "#F27405", "#F7F2B2", "#203759"];

export const SIZES = {
  small: 10,
  medium: 14,
  large: 18,
};

export function randomBoard(size, colours = PALETTE.length, rng = Math.random) {
  return Array.from({ length: size * size }, () => Math.floor(rng() * colours));
}

/** Indices of the region connected to the top-left cell by matching colour. */
export function floodRegion(cells, size) {
  const colour = cells[0];
  const seen = new Uint8Array(cells.length);
  const stack = [0];
  const region = [];
  seen[0] = 1;
  while (stack.length) {
    const i = stack.pop();
    region.push(i);
    const r = Math.floor(i / size);
    const c = i % size;
    const next = [];
    if (r > 0) next.push(i - size);
    if (r < size - 1) next.push(i + size);
    if (c > 0) next.push(i - 1);
    if (c < size - 1) next.push(i + 1);
    for (const n of next) {
      if (!seen[n] && cells[n] === colour) {
        seen[n] = 1;
        stack.push(n);
      }
    }
  }
  return region;
}

/** Returns a new board with the flooded region recoloured. */
export function flood(cells, size, colour) {
  const next = cells.slice();
  for (const i of floodRegion(cells, size)) next[i] = colour;
  return next;
}

export function isComplete(cells) {
  return cells.every((c) => c === cells[0]);
}

/**
 * Greedy solver: each step picks the colour that grows the flood the most
 * (ties broken by lowest index). Its move count is always achievable, so it
 * works as a fair move limit.
 */
export function greedyMoves(cells, size, colours = PALETTE.length) {
  let board = cells;
  let moves = 0;
  while (!isComplete(board)) {
    let best = null;
    let bestSize = -1;
    for (let c = 0; c < colours; c++) {
      if (c === board[0]) continue;
      const candidate = flood(board, size, c);
      const grown = floodRegion(candidate, size).length;
      if (grown > bestSize) {
        bestSize = grown;
        best = candidate;
      }
    }
    board = best;
    moves++;
  }
  return moves;
}

/**
 * Game state helper. `play` ignores the current flood colour (it would waste
 * a move) and anything after the game has ended.
 */
export function createGame(size, rng = Math.random, colours = PALETTE.length) {
  const cells = randomBoard(size, colours, rng);
  return {
    size,
    colours,
    initial: cells.slice(),
    cells,
    moves: 0,
    limit: greedyMoves(cells, size, colours),
    status: "playing", // playing | won | lost
  };
}

export function restart(game) {
  return { ...game, cells: game.initial.slice(), moves: 0, status: "playing" };
}

export function play(game, colour) {
  if (game.status !== "playing" || colour === game.cells[0]) return game;
  const cells = flood(game.cells, game.size, colour);
  const moves = game.moves + 1;
  let status = "playing";
  if (isComplete(cells)) status = "won";
  else if (moves >= game.limit) status = "lost";
  return { ...game, cells, moves, status };
}
