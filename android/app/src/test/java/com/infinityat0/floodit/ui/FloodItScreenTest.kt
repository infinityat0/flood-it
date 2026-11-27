package com.infinityat0.floodit.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.infinityat0.floodit.game.Size
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * Drives the real Compose UI on the JVM. Screenshots land in
 * app/build/screenshots/ for a quick visual check without an emulator.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class FloodItScreenTest {

    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    /** Renders the screen the way MainActivity does, including the saved theme/appearance. */
    private fun launch(): FloodItViewModel {
        val vm = FloodItViewModel(compose.activity.application)
        compose.setContent {
            val dark = vm.appearance == Appearance.DARK
            FloodItTheme(dark = dark, colorTheme = vm.colorTheme) { FloodItScreen(vm) }
        }
        return vm
    }

    private fun currentColor(names: List<String>): String = names.first {
        compose.onAllNodesWithContentDescription("$it, current color").fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun makingAMoveUpdatesTheScreen() {
        val vm = launch()
        vm.newGame(Size.SMALL)
        compose.onNodeWithText("Tap a color below to start flooding.").assertExists()
        snapshot("start")

        val names = vm.colorTheme.colorNames
        val target = names.first { it != currentColor(names) }
        compose.onNodeWithContentDescription(target).performClick()
        compose.waitForIdle()
        check(currentColor(names) == target) { "flood color should become $target" }
        compose.onNodeWithText("Tap a color below to start flooding.").assertDoesNotExist()
        snapshot("after-move")
    }

    @Test
    fun menuChangesThemeAppearanceAndSize() {
        val vm = launch()
        compose.onNodeWithContentDescription("Open menu").performClick()
        compose.waitForIdle()
        snapshot("menu")

        compose.onNodeWithText("Ocean").performClick()
        compose.onNodeWithText("Dark").performClick()
        compose.onNodeWithText("18×18").performScrollTo().performClick()
        compose.waitForIdle()
        assertEquals("ocean", vm.colorTheme.id)
        assertEquals(Appearance.DARK, vm.appearance)
        assertEquals(18, vm.game.size)
        snapshot("menu-dark-ocean")

        compose.activity.onBackPressedDispatcher.onBackPressed()
        compose.waitForIdle()
        compose.onNodeWithContentDescription(vm.colorTheme.colorNames.first { it != currentColor(vm.colorTheme.colorNames) })
            .performClick()
        snapshot("large-dark-ocean")
    }

    @Test
    fun settingsSurviveARestart() {
        val first = FloodItViewModel(compose.activity.application)
        first.selectColorTheme(COLOR_THEMES.first { it.id == "sunset" })
        first.selectAppearance(Appearance.LIGHT)
        first.newGame(Size.LARGE)

        // A fresh ViewModel reads the same saved settings, as after an app restart.
        val second = FloodItViewModel(compose.activity.application)
        assertEquals("sunset", second.colorTheme.id)
        assertEquals(Appearance.LIGHT, second.appearance)
        assertEquals(Size.LARGE, second.size)
        assertEquals(18, second.game.size)
    }

    @Test
    fun howToPlayOpensFromTheMenu() {
        launch()
        compose.onNodeWithContentDescription("Open menu").performClick()
        compose.onNodeWithText("How to play").performScrollTo().performClick()
        compose.onNodeWithText("Your flood starts in the top-left corner.").assertExists()
        snapshot("how-to-play")
    }

    /** Draws the activity's window into a PNG (captureToImage() hangs under Robolectric). */
    private fun snapshot(name: String) {
        compose.waitForIdle()
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/screenshots").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
