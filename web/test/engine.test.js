import { test } from "node:test";
import assert from "node:assert/strict";
import {
  PALETTE,
  createGame,
  flood,
  floodRegion,
  greedyMoves,
  isComplete,
  play,
  randomBoard,
  restart,
} from "../public/engine.js";

test("random boards only use palette colours, including the first", () => {
  const seen = new Set(randomBoard(20));
  assert.equal(seen.size, PALETTE.length, "all six colours appear");
  for (const c of seen) assert.ok(c >= 0 && c < PALETTE.length);
});

test("flood region follows matching colours orthogonally only", () => {
  // 3x3:
  // 0 0 1
  // 1 0 1
  // 0 1 0   <- (2,0) is 0 but only diagonally connected
  const cells = [0, 0, 1, 1, 0, 1, 0, 1, 0];
  assert.deepEqual(floodRegion(cells, 3).sort(), [0, 1, 4]);
});

test("flooding recolours the region and absorbs matching neighbours", () => {
  const cells = [0, 0, 1, 1, 0, 1, 0, 1, 0];
  const next = flood(cells, 3, 1);
  assert.deepEqual(next, [1, 1, 1, 1, 1, 1, 0, 1, 0]);
  assert.equal(floodRegion(next, 3).length, 7);
});

test("greedy solver always finishes and gives a sane limit", () => {
  for (let i = 0; i < 20; i++) {
    const g = createGame(14);
    assert.ok(g.limit > 5 && g.limit < 60, `limit ${g.limit}`);
  }
});

test("playing the greedy line wins within the limit", () => {
  const g0 = createGame(10);
  let g = g0;
  while (g.status === "playing") {
    // Re-run greedy one step at a time.
    let best = null;
    let bestSize = -1;
    for (let c = 0; c < PALETTE.length; c++) {
      if (c === g.cells[0]) continue;
      const size = floodRegion(flood(g.cells, 10, c), 10).length;
      if (size > bestSize) [best, bestSize] = [c, size];
    }
    g = play(g, best);
  }
  assert.equal(g.status, "won");
  assert.equal(g.moves, g0.limit);
});

test("choosing the current colour does not cost a move", () => {
  const g = createGame(10);
  assert.equal(play(g, g.cells[0]).moves, 0);
});

test("running out of moves loses and locks the board", () => {
  let g = { ...createGame(10), limit: 1 };
  const other = (g.cells[0] + 1) % PALETTE.length;
  g = play(g, other);
  if (!isComplete(g.cells)) {
    assert.equal(g.status, "lost");
    const after = play(g, (other + 1) % PALETTE.length);
    assert.equal(after, g, "no moves after losing");
  }
});

test("restart returns to the same starting board", () => {
  const g = createGame(10);
  const played = play(g, (g.cells[0] + 1) % PALETTE.length);
  const r = restart(played);
  assert.deepEqual(r.cells, g.initial);
  assert.equal(r.moves, 0);
  assert.equal(r.limit, g.limit);
});

test("a single-colour board is already complete", () => {
  assert.equal(isComplete([2, 2, 2, 2]), true);
  assert.equal(greedyMoves([2, 2, 2, 2], 2), 0);
});
