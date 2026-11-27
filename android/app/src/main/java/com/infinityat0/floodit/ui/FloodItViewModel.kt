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

/** Light/dark choice. SYSTEM follows the phone's setting. */
enum class Appearance(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

private const val START_MESSAGE = "Tap a color below to start flooding."

/**
 * Holds the game across configuration changes, plus the player's settings.
 *
 * Settings live in the "settings" SharedPreferences file, which survives restarts
 * and app updates, and is included in Android Auto Backup (see res/xml/backup_rules.xml
 * and data_extraction_rules.xml) so it comes back after a reinstall.
 */
class FloodItViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)

    var colorTheme by mutableStateOf(colorThemeById(prefs.getString(KEY_THEME, null)))
        private set

    var appearance by mutableStateOf(
        enumOrDefault(prefs.getString(KEY_APPEARANCE, null), Appearance.SYSTEM),
    )
        private set

    var size by mutableStateOf(enumOrDefault(prefs.getString(KEY_SIZE, null), Size.MEDIUM))
        private set

    var game by mutableStateOf(createGame(size.cells))
        private set

    var message by mutableStateOf(Message(START_MESSAGE, MessageKind.INFO))
        private set

    /** Percentage of the board flooded (never below 1%). */
    val filledPercent: Int
        get() = maxOf(1, floodRegion(game.cells, game.size).size * 100 / game.cells.size)

    fun selectColorTheme(theme: ColorTheme) {
        colorTheme = theme
        prefs.edit { putString(KEY_THEME, theme.id) }
    }

    fun selectAppearance(value: Appearance) {
        appearance = value
        prefs.edit { putString(KEY_APPEARANCE, value.name) }
    }

    fun newGame(newSize: Size = size) {
        if (newSize != size) {
            size = newSize
            prefs.edit { putString(KEY_SIZE, newSize.name) }
        }
        game = createGame(newSize.cells)
        message = Message(START_MESSAGE, MessageKind.INFO)
    }

    fun retry() {
        game = restart(game)
        message = Message("Same board, fresh start.", MessageKind.INFO)
    }

    fun choose(color: Int) {
        val before = game
        game = play(before, color)
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
        const val PREFS_FILE = "settings"
        const val KEY_THEME = "color_theme"
        const val KEY_APPEARANCE = "appearance"
        const val KEY_SIZE = "size"

        inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
            enumValues<T>().firstOrNull { it.name == name } ?: default
    }
}
