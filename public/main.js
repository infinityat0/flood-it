import { PALETTE, SIZES, createGame, floodRegion, play, restart } from "./engine.js";

const NAMES = ["Plum", "Rose", "Moss", "Orange", "Cream", "Navy"];

const $ = (id) => document.getElementById(id);
const boardEl = $("board");
const paletteEl = $("palette");
const sizeEl = $("size");
const movesEl = $("moves");
const limitEl = $("limit");
const filledEl = $("filled");
const messageEl = $("message");
const retryBtn = $("retry");

let game;
let cellEls = [];

function setMessage(text, kind = "") {
  messageEl.textContent = text;
  messageEl.className = `message ${kind}`;
}

function buildPalette() {
  PALETTE.forEach((colour, i) => {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "swatch";
    btn.style.background = colour;
    btn.dataset.colour = i;
    btn.setAttribute("aria-label", `${NAMES[i]} (key ${i + 1})`);
    btn.innerHTML = `<kbd>${i + 1}</kbd>`;
    paletteEl.append(btn);
  });
}

function buildBoard() {
  boardEl.replaceChildren();
  boardEl.style.setProperty("--size", game.size);
  cellEls = game.cells.map(() => {
    const span = document.createElement("span");
    boardEl.append(span);
    return span;
  });
}

function newBoard() {
  try {
    localStorage.setItem("flood-it.size", sizeEl.value);
  } catch {
    /* storage unavailable */
  }
  game = createGame(SIZES[sizeEl.value]);
  buildBoard();
  setMessage("Pick a colour to start flooding.");
  render();
}

function render() {
  game.cells.forEach((c, i) => {
    cellEls[i].style.backgroundColor = PALETTE[c];
  });
  const filled = floodRegion(game.cells, game.size).length;
  movesEl.textContent = game.moves;
  limitEl.textContent = game.limit;
  filledEl.textContent = `${Math.max(1, Math.floor((filled / game.cells.length) * 100))}%`;
  boardEl.classList.toggle("won", game.status === "won");
  for (const btn of paletteEl.children) {
    const c = Number(btn.dataset.colour);
    btn.classList.toggle("current", c === game.cells[0]);
    btn.disabled = game.status !== "playing";
    btn.setAttribute("aria-pressed", String(c === game.cells[0]));
  }
  boardEl.setAttribute("aria-label", `Colour board, ${filledEl.textContent} flooded`);
}

function choose(colour) {
  const before = game;
  game = play(game, colour);
  if (game === before) return;
  if (game.status === "won") {
    const spare = game.limit - game.moves;
    setMessage(
      spare > 0 ? `Flooded in ${game.moves} moves, ${spare} to spare!` : `Flooded with the last move!`,
      "win",
    );
  } else if (game.status === "lost") {
    setMessage("Out of moves. Retry this board or start a new one.", "lose");
  } else {
    const left = game.limit - game.moves;
    setMessage(left <= 3 ? `${left} move${left === 1 ? "" : "s"} left.` : "");
  }
  render();
}

paletteEl.addEventListener("click", (e) => {
  const btn = e.target.closest(".swatch");
  if (btn) choose(Number(btn.dataset.colour));
});

document.addEventListener("keydown", (e) => {
  if (e.ctrlKey || e.metaKey || e.altKey) return;
  const n = Number(e.key);
  if (n >= 1 && n <= PALETTE.length) choose(n - 1);
});

retryBtn.addEventListener("click", () => {
  game = restart(game);
  setMessage("Same board, fresh start.");
  render();
});
$("new").addEventListener("click", newBoard);
sizeEl.addEventListener("change", newBoard);

buildPalette();
try {
  const saved = localStorage.getItem("flood-it.size");
  if (saved && SIZES[saved]) sizeEl.value = saved;
} catch {
  /* storage unavailable */
}
newBoard();
