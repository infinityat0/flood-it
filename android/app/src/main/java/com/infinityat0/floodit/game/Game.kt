package com.infinityat0.floodit.game

import kotlin.random.Random

/** Pure rules for the flood fill puzzle, ported from the web engine.js. */

/** Palette from the web version, as 0xAARRGGBB ints (no Android imports here). */
val PALETTE: List<Long> = listOf(0xFF541E32, 0xFF8E3557, 0xFF88A33E, 0xFFF27405, 0xFFF7F2B2, 0xFF203759)
val COLOUR_NAMES: List<String> = listOf("Plum", "Rose", "Moss", "Orange", "Cream", "Navy")

enum class Size(val label: String, val cells: Int) {
    SMALL("Small", 10),
    MEDIUM("Medium", 14),
    LARGE("Large", 18),
}

enum class Status { PLAYING, WON, LOST }

fun randomBoard(size: Int, colours: Int = PALETTE.size, rng: Random = Random.Default): IntArray =
    IntArray(size * size) { rng.nextInt(colours) }

/** Indices of the region connected to the top-left cell by matching colour. */
fun floodRegion(cells: IntArray, size: Int): List<Int> {
    val colour = cells[0]
    val seen = BooleanArray(cells.size)
    val stack = ArrayDeque<Int>()
    val region = ArrayList<Int>()
    stack.add(0)
    seen[0] = true
    while (stack.isNotEmpty()) {
        val i = stack.removeLast()
        region.add(i)
        val r = i / size
        val c = i % size
        val next = ArrayList<Int>(4)
        if (r > 0) next.add(i - size)
        if (r < size - 1) next.add(i + size)
        if (c > 0) next.add(i - 1)
        if (c < size - 1) next.add(i + 1)
        for (n in next) {
            if (!seen[n] && cells[n] == colour) {
                seen[n] = true
                stack.add(n)
            }
        }
    }
    return region
}

/** Returns a new board with the flooded region recoloured. */
fun flood(cells: IntArray, size: Int, colour: Int): IntArray {
    val next = cells.copyOf()
    for (i in floodRegion(cells, size)) next[i] = colour
    return next
}

fun isComplete(cells: IntArray): Boolean = cells.all { it == cells[0] }

/**
 * Greedy solver: each step picks the colour that grows the flood the most
 * (ties broken by lowest index). Its move count is always achievable.
 */
fun greedyMoves(cells: IntArray, size: Int, colours: Int = PALETTE.size): Int {
    var board = cells
    var moves = 0
    while (!isComplete(board)) {
        var best: IntArray? = null
        var bestSize = -1
        for (c in 0 until colours) {
            if (c == board[0]) continue
            val candidate = flood(board, size, c)
            val grown = floodRegion(candidate, size).size
            if (grown > bestSize) {
                bestSize = grown
                best = candidate
            }
        }
        board = best!!
        moves++
    }
    return moves
}

/** Immutable game state. [play] ignores the current flood colour and anything after the end. */
class Game(
    val size: Int,
    val colours: Int,
    val initial: IntArray,
    val cells: IntArray,
    val moves: Int,
    val limit: Int,
    val status: Status,
) {
    fun copy(cells: IntArray = this.cells, moves: Int = this.moves, status: Status = this.status) =
        Game(size, colours, initial, cells, moves, limit, status)
}

fun createGame(size: Int, rng: Random = Random.Default, colours: Int = PALETTE.size): Game {
    val cells = randomBoard(size, colours, rng)
    return Game(size, colours, cells.copyOf(), cells, 0, greedyMoves(cells, size, colours), Status.PLAYING)
}

fun restart(game: Game): Game = game.copy(cells = game.initial.copyOf(), moves = 0, status = Status.PLAYING)

fun play(game: Game, colour: Int): Game {
    if (game.status != Status.PLAYING || colour == game.cells[0]) return game
    val cells = flood(game.cells, game.size, colour)
    val moves = game.moves + 1
    val status = when {
        isComplete(cells) -> Status.WON
        moves >= game.limit -> Status.LOST
        else -> Status.PLAYING
    }
    return game.copy(cells = cells, moves = moves, status = status)
}
