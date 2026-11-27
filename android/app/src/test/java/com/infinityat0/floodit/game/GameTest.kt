package com.infinityat0.floodit.game

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTest {
    @Test
    fun randomBoardsOnlyUsePaletteColoursIncludingTheFirst() {
        val seen = randomBoard(20).toSet()
        assertEquals("all six colours appear", COLOR_COUNT, seen.size)
        for (c in seen) assertTrue(c in 0 until COLOR_COUNT)
    }

    @Test
    fun floodRegionFollowsMatchingColoursOrthogonallyOnly() {
        // (2,0) is 0 but only diagonally connected
        val cells = intArrayOf(0, 0, 1, 1, 0, 1, 0, 1, 0)
        assertEquals(listOf(0, 1, 4), floodRegion(cells, 3).sorted())
    }

    @Test
    fun floodingRecoloursTheRegionAndAbsorbsMatchingNeighbours() {
        val cells = intArrayOf(0, 0, 1, 1, 0, 1, 0, 1, 0)
        val next = flood(cells, 3, 1)
        assertArrayEquals(intArrayOf(1, 1, 1, 1, 1, 1, 0, 1, 0), next)
        assertEquals(7, floodRegion(next, 3).size)
    }

    @Test
    fun greedySolverAlwaysFinishesAndGivesASaneLimit() {
        repeat(20) {
            val g = createGame(14)
            assertTrue("limit ${g.limit}", g.limit > 5 && g.limit < 60)
        }
    }

    @Test
    fun playingTheGreedyLineWinsWithinTheLimit() {
        val g0 = createGame(10)
        var g = g0
        while (g.status == Status.PLAYING) {
            var best = -1
            var bestSize = -1
            for (c in 0 until COLOR_COUNT) {
                if (c == g.cells[0]) continue
                val size = floodRegion(flood(g.cells, 10, c), 10).size
                if (size > bestSize) {
                    best = c
                    bestSize = size
                }
            }
            g = play(g, best)
        }
        assertEquals(Status.WON, g.status)
        assertEquals(g0.limit, g.moves)
    }

    @Test
    fun choosingTheCurrentColourDoesNotCostAMove() {
        val g = createGame(10)
        assertEquals(0, play(g, g.cells[0]).moves)
    }

    @Test
    fun runningOutOfMovesLosesAndLocksTheBoard() {
        val base = createGame(10)
        var g = Game(base.size, base.colours, base.initial, base.cells, 0, 1, Status.PLAYING)
        val other = (g.cells[0] + 1) % COLOR_COUNT
        g = play(g, other)
        if (!isComplete(g.cells)) {
            assertEquals(Status.LOST, g.status)
            val after = play(g, (other + 1) % COLOR_COUNT)
            assertSame("no moves after losing", g, after)
        }
    }

    @Test
    fun restartReturnsToTheSameStartingBoard() {
        val g = createGame(10)
        val played = play(g, (g.cells[0] + 1) % COLOR_COUNT)
        val r = restart(played)
        assertArrayEquals(g.initial, r.cells)
        assertEquals(0, r.moves)
        assertEquals(g.limit, r.limit)
    }

    @Test
    fun aSingleColourBoardIsAlreadyComplete() {
        assertTrue(isComplete(intArrayOf(2, 2, 2, 2)))
        assertEquals(0, greedyMoves(intArrayOf(2, 2, 2, 2), 2))
        assertFalse(isComplete(intArrayOf(2, 2, 2, 1)))
    }
}
