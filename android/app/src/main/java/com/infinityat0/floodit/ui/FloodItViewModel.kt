package com.infinityat0.floodit.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.infinityat0.floodit.game.Game
import com.infinityat0.floodit.game.Size
import com.infinityat0.floodit.game.Status
import com.infinityat0.floodit.game.createGame
import com.infinityat0.floodit.game.floodRegion
import com.infinityat0.floodit.game.play
import com.infinityat0.floodit.game.restart

enum class MessageKind { NONE, INFO, WIN, LOSE }

class Message(val text: String, val kind: MessageKind)

/** Holds the game across configuration changes and remembers the board size. */
class FloodItViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("flood-it", Context.MODE_PRIVATE)

    var size by mutableStateOf(
        runCatching { Size.valueOf(prefs.getString(KEY_SIZE, null)!!) }.getOrDefault(Size.MEDIUM),
    )
        private set

    var game by mutableStateOf(createGame(size.cells))
        private set

    var message by mutableStateOf(Message("Pick a colour to start flooding.", MessageKind.INFO))
        private set

    /** Percentage of the board flooded, as the web shows it (never below 1%). */
    val filledPercent: Int
        get() = maxOf(1, floodRegion(game.cells, game.size).size * 100 / game.cells.size)

    fun newGame(newSize: Size = size) {
        size = newSize
        prefs.edit { putString(KEY_SIZE, newSize.name) }
        game = createGame(newSize.cells)
        message = Message("Pick a colour to start flooding.", MessageKind.INFO)
    }

    fun retry() {
        game = restart(game)
        message = Message("Same board, fresh start.", MessageKind.INFO)
    }

    fun choose(colour: Int) {
        val before = game
        game = play(before, colour)
        if (game === before) return
        message = when (game.status) {
            Status.WON -> {
                val spare = game.limit - game.moves
                Message(
                    if (spare > 0) "Flooded in ${game.moves} moves, $spare to spare!" else "Flooded with the last move!",
                    MessageKind.WIN,
                )
            }
            Status.LOST -> Message("Out of moves. Retry this board or start a new one.", MessageKind.LOSE)
            Status.PLAYING -> {
                val left = game.limit - game.moves
                Message(if (left <= 3) "$left move${if (left == 1) "" else "s"} left." else "", MessageKind.INFO)
            }
        }
    }

    private companion object {
        const val KEY_SIZE = "size"
    }
}
