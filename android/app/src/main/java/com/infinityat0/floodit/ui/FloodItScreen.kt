package com.infinityat0.floodit.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GeoSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinityat0.floodit.game.COLOUR_NAMES
import com.infinityat0.floodit.game.Game
import com.infinityat0.floodit.game.PALETTE
import com.infinityat0.floodit.game.Size
import com.infinityat0.floodit.game.Status

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodItScreen(vm: FloodItViewModel = viewModel()) {
    var showRules by rememberSaveable { mutableStateOf(false) }
    val game = vm.game
    val haptics = LocalHapticFeedback.current
    val colors = LocalFloodColors.current

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Flood It", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Size.entries.forEachIndexed { i, size ->
                    SegmentedButton(
                        selected = vm.size == size,
                        onClick = { vm.newGame(size) },
                        shape = SegmentedButtonDefaults.itemShape(i, Size.entries.size),
                    ) { Text("${size.label} ${size.cells}×${size.cells}", maxLines = 1, fontSize = 12.sp) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Stat("Moves", game.moves.toString())
                Stat("Limit", game.limit.toString())
                Stat("Filled", "${vm.filledPercent}%")
            }

            BoardCanvas(game, vm.filledPercent)

            Palette(game) { colour ->
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                vm.choose(colour)
                if (vm.game.status != Status.PLAYING) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            val msgColor = when (vm.message.kind) {
                MessageKind.WIN -> colors.good
                MessageKind.LOSE -> colors.bad
                else -> MaterialTheme.colorScheme.onBackground
            }
            Text(
                vm.message.text,
                color = msgColor,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.height(48.dp).fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = vm::retry) { Text("Retry this board") }
                Button(onClick = { vm.newGame() }) { Text("New board") }
            }
            TextButton(onClick = { showRules = true }) { Text("How to play") }
        }
    }

    if (showRules) RulesDialog(onDismiss = { showRules = false })
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LocalFloodColors.current.muted,
            letterSpacing = 1.sp,
        )
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

/** The board is drawn on one Canvas, so 18x18 (324 cells) stays cheap. */
@Composable
private fun BoardCanvas(game: Game, filledPercent: Int) {
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, 480.dp)
        Canvas(
            Modifier
                .size(side)
                .clip(RoundedCornerShape(12.dp))
                .semantics { contentDescription = "Colour board, $filledPercent% flooded" },
        ) {
            val cell = this.size.width / game.size
            val cells = game.cells
            for (i in cells.indices) {
                val r = i / game.size
                val c = i % game.size
                // Overdraw by a pixel to avoid hairline seams between cells.
                drawRect(
                    color = Color(PALETTE[cells[i]]),
                    topLeft = Offset(c * cell, r * cell),
                    size = GeoSize(cell + 1f, cell + 1f),
                )
            }
        }
    }
}

@Composable
private fun Palette(game: Game, onPick: (Int) -> Unit) {
    val enabled = game.status == Status.PLAYING
    val outline = MaterialTheme.colorScheme.onBackground
    Row(
        Modifier.fillMaxWidth().widthIn(max = 480.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        PALETTE.forEachIndexed { i, colour ->
            val current = i == game.cells[0]
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .alpha(if (enabled) 1f else 0.35f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(colour))
                    .then(
                        if (current) {
                            Modifier.border(BorderStroke(3.dp, outline), RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        },
                    )
                    .clickable(enabled = enabled) { onPick(i) }
                    .semantics {
                        contentDescription = COLOUR_NAMES[i] + if (current) ", current colour" else ""
                    },
            )
        }
    }
}

@Composable
private fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } },
        title = { Text("How to play") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "Your flood starts in the top-left square.",
                    "Pick a colour: the flood turns that colour and swallows every touching square of it.",
                    "Fill the entire board within the move limit to win. Every board can be won within its limit.",
                ).forEach { Text("• $it") }
                Spacer(Modifier.height(4.dp))
            }
        },
    )
}
