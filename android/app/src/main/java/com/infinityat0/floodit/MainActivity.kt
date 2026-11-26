package com.infinityat0.floodit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.infinityat0.floodit.ui.FloodItScreen
import com.infinityat0.floodit.ui.FloodItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FloodItTheme {
                FloodItScreen()
            }
        }
    }
}
