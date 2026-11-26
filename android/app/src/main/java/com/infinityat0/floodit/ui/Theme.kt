package com.infinityat0.floodit.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Colours taken from the web version's base.css and style.css. */
@Immutable
data class FloodColors(
    val muted: Color,
    val good: Color,
    val bad: Color,
)

private val LightColors = FloodColors(
    muted = Color(0xFF756C62),
    good = Color(0xFF2E7D4F),
    bad = Color(0xFFB2392F),
)

private val DarkColors = FloodColors(
    muted = Color(0xFFA19A91),
    good = Color(0xFF6CC48F),
    bad = Color(0xFFEF7B70),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF8E3557),
    onPrimary = Color.White,
    background = Color(0xFFF4F1EA),
    onBackground = Color(0xFF24211D),
    surface = Color(0xFFF4F1EA),
    onSurface = Color(0xFF24211D),
    surfaceContainer = Color(0xFFFFFDF8),
    outline = Color(0xFFDCD3C6),
    error = Color(0xFFB2392F),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFFE07CA3),
    onPrimary = Color(0xFF10151A),
    background = Color(0xFF181A1D),
    onBackground = Color(0xFFE9E6E1),
    surface = Color(0xFF181A1D),
    onSurface = Color(0xFFE9E6E1),
    surfaceContainer = Color(0xFF22252A),
    outline = Color(0xFF3A3E45),
    error = Color(0xFFEF7B70),
)

val LocalFloodColors = staticCompositionLocalOf { LightColors }

@Composable
fun FloodItTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFloodColors provides if (dark) DarkColors else LightColors) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
    }
}
