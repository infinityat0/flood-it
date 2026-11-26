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

    private val names = listOf("Plum", "Rose", "Moss", "Orange", "Cream", "Navy")

    private fun currentColour(): String = names.first {
        compose.onAllNodesWithContentDescription("$it, current colour").fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun makingAMoveUpdatesTheScreen() {
        compose.setContent { FloodItTheme { FloodItScreen() } }
        compose.onNodeWithText("Small 10×10").performClick()
        compose.onNodeWithText("Pick a colour to start flooding.").assertExists()
        snapshot("start")

        val before = currentColour()
        val target = names.first { it != before }
        compose.onNodeWithContentDescription(target).performClick()
        compose.waitForIdle()
        check(currentColour() == target) { "flood colour should become $target" }
        compose.onNodeWithText("Pick a colour to start flooding.").assertDoesNotExist()
        snapshot("after-move")
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-night-xxhdpi")
    fun largeBoardInDarkMode() {
        compose.setContent { FloodItTheme(dark = true) { FloodItScreen() } }
        compose.onNodeWithText("Large 18×18").performClick()
        compose.waitForIdle()
        val target = names.first { it != currentColour() }
        compose.onNodeWithContentDescription(target).performClick()
        snapshot("large-dark")
        compose.onNodeWithText("Medium 14×14").performClick()
    }

    @Test
    fun rulesDialogOpens() {
        compose.setContent { FloodItTheme { FloodItScreen() } }
        compose.onNodeWithText("How to play").performScrollTo().performClick()
        compose.onNodeWithText("Your flood starts in the top-left square.", substring = true).assertExists()
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
