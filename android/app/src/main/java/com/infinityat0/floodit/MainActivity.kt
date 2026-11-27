package com.infinityat0.floodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import com.infinityat0.floodit.ui.Appearance
import com.infinityat0.floodit.ui.FloodItScreen
import com.infinityat0.floodit.ui.FloodItTheme
import com.infinityat0.floodit.ui.FloodItViewModel

class MainActivity : ComponentActivity() {
    private val vm: FloodItViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val dark = when (vm.appearance) {
                Appearance.SYSTEM -> isSystemInDarkTheme()
                Appearance.LIGHT -> false
                Appearance.DARK -> true
            }
            // Keep status/navigation bar icons readable when Light/Dark is forced in Settings.
            LaunchedEffect(dark) {
                val style = SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ) { dark }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            FloodItTheme(dark = dark, colorTheme = vm.colorTheme) {
                FloodItScreen(vm)
            }
        }
    }
}
