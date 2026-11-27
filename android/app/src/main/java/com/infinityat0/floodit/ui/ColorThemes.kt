package com.infinityat0.floodit.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.infinityat0.floodit.game.COLOR_COUNT

/** A named set of six tile colours. [id] is what gets saved, so never rename one. */
@Immutable
data class ColorTheme(
    val id: String,
    val name: String,
    val colors: List<Color>,
    /** Spoken names for screen readers, one per colour. */
    val colorNames: List<String>,
) {
    init {
        require(colors.size == COLOR_COUNT && colorNames.size == COLOR_COUNT)
    }
}

private fun theme(id: String, name: String, vararg swatches: Pair<Long, String>) =
    ColorTheme(id, name, swatches.map { Color(it.first) }, swatches.map { it.second })

/**
 * Each set keeps six hues far apart in both hue and lightness, so neighbouring
 * regions stay easy to tell apart.
 */
val COLOR_THEMES: List<ColorTheme> = listOf(
    theme(
        "meadow", "Meadow",
        0xFFEF8A7B to "Coral", 0xFFF4C95D to "Sunflower", 0xFF7CC6A4 to "Mint",
        0xFF6AA6DA to "Sky", 0xFFA891D8 to "Lilac", 0xFF4F5D75 to "Slate",
    ),
    theme(
        "ocean", "Ocean",
        0xFF1D3557 to "Navy", 0xFF457B9D to "Steel blue", 0xFF2A9D8F to "Teal",
        0xFFA8DADC to "Sea foam", 0xFFE9C46A to "Sand", 0xFFE76F51 to "Coral",
    ),
    theme(
        "sunset", "Sunset",
        0xFF355070 to "Dusk", 0xFF6D597A to "Plum", 0xFFB56576 to "Mauve",
        0xFFE56B6F to "Salmon", 0xFFEAAC8B to "Apricot", 0xFFF6D186 to "Gold",
    ),
    theme(
        "pastel", "Pastel",
        0xFFFFADAD to "Pink", 0xFFFFD6A5 to "Peach", 0xFFFDFFB6 to "Lemon",
        0xFFCAFFBF to "Mint", 0xFF9BF6FF to "Aqua", 0xFFBDB2FF to "Periwinkle",
    ),
    theme(
        "classic", "Classic",
        0xFF541E32 to "Plum", 0xFF8E3557 to "Rose", 0xFF88A33E to "Moss",
        0xFFF27405 to "Orange", 0xFFF7F2B2 to "Cream", 0xFF203759 to "Navy",
    ),
    // Okabe–Ito palette: distinguishable with the common forms of color blindness.
    theme(
        "accessible", "Color-blind friendly",
        0xFFE69F00 to "Orange", 0xFF56B4E9 to "Sky blue", 0xFF009E73 to "Green",
        0xFFF0E442 to "Yellow", 0xFF0072B2 to "Blue", 0xFFCC79A7 to "Pink",
    ),
)

val DEFAULT_COLOR_THEME: ColorTheme = COLOR_THEMES.first()

fun colorThemeById(id: String?): ColorTheme = COLOR_THEMES.firstOrNull { it.id == id } ?: DEFAULT_COLOR_THEME
