package com.infinityat0.floodit.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as GeoSize
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.infinityat0.floodit.game.Game
import com.infinityat0.floodit.game.Size
import com.infinityat0.floodit.game.Status
import kotlinx.coroutines.launch

const val APP_TITLE = "Color Flood"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloodItScreen(vm: FloodItViewModel = viewModel()) {
    var showRules by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val game = vm.game
    val haptics = LocalHapticFeedback.current
    val colors = LocalFloodColors.current

    // Back closes the menu before it leaves the app.
    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SettingsDrawer(
                vm = vm,
                onHowToPlay = {
                    scope.launch { drawerState.close() }
                    showRules = true
                },
            )
        },
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(APP_TITLE, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.semantics { contentDescription = "Open menu" },
                        ) { MenuIcon() }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    ),
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    Stat("Moves", "${game.moves} / ${game.limit}")
                    Stat("Filled", "${vm.filledPercent}%")
                    Stat("Board", "${game.size}×${game.size}")
                }

                BoardCanvas(game, vm.filledPercent)

                Palette(game) { color ->
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    vm.choose(color)
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
                    OutlinedButton(onClick = vm::retry) { Text("Retry board") }
                    Button(onClick = { vm.newGame() }) { Text("New board") }
                }
            }
        }
    }

    if (showRules) RulesDialog(onDismiss = { showRules = false })
}

/** Three-line "hamburger" icon, drawn so we don't need the icons library. */
@Composable
private fun MenuIcon() {
    val color = LocalContentColor.current
    Canvas(Modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        val left = 3.dp.toPx()
        val right = size.width - 3.dp.toPx()
        for (y in listOf(6.dp, 12.dp, 18.dp)) {
            drawLine(color, Offset(left, y.toPx()), Offset(right, y.toPx()), stroke, StrokeCap.Round)
        }
    }
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
    val palette = LocalColorTheme.current.colors
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, 480.dp)
        Canvas(
            Modifier
                .size(side)
                .clip(RoundedCornerShape(16.dp))
                .semantics { contentDescription = "Color board, $filledPercent% flooded" },
        ) {
            val cell = this.size.width / game.size
            val cells = game.cells
            for (i in cells.indices) {
                val r = i / game.size
                val c = i % game.size
                // Overdraw by a pixel to avoid hairline seams between cells.
                drawRect(
                    color = palette[cells[i]],
                    topLeft = Offset(c * cell, r * cell),
                    size = GeoSize(cell + 1f, cell + 1f),
                )
            }
        }
    }
}

@Composable
private fun Palette(game: Game, onPick: (Int) -> Unit) {
    val theme = LocalColorTheme.current
    val enabled = game.status == Status.PLAYING
    val outline = MaterialTheme.colorScheme.onBackground
    Row(
        Modifier.fillMaxWidth().widthIn(max = 480.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        theme.colors.forEachIndexed { i, color ->
            val current = i == game.cells[0]
            Box(
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .alpha(if (enabled) 1f else 0.35f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color)
                    .then(
                        if (current) {
                            Modifier.border(BorderStroke(3.dp, outline), RoundedCornerShape(14.dp))
                        } else {
                            Modifier
                        },
                    )
                    .clickable(enabled = enabled, role = Role.Button) { onPick(i) }
                    .semantics {
                        contentDescription = theme.colorNames[i] + if (current) ", current color" else ""
                    },
            )
        }
    }
}

@Composable
private fun SettingsDrawer(vm: FloodItViewModel, onHowToPlay: () -> Unit) {
    ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(APP_TITLE, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            SectionTitle("Colors")
            COLOR_THEMES.forEach { theme ->
                ThemeRow(theme, selected = theme.id == vm.colorTheme.id) { vm.selectColorTheme(theme) }
            }

            SectionTitle("Appearance")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Appearance.entries.forEachIndexed { i, value ->
                    SegmentedButton(
                        selected = vm.appearance == value,
                        onClick = { vm.selectAppearance(value) },
                        shape = SegmentedButtonDefaults.itemShape(i, Appearance.entries.size),
                    ) { Text(value.label) }
                }
            }

            SectionTitle("Board size")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Size.entries.forEachIndexed { i, size ->
                    SegmentedButton(
                        selected = vm.size == size,
                        onClick = { vm.newGame(size) },
                        shape = SegmentedButtonDefaults.itemShape(i, Size.entries.size),
                    ) { Text("${size.cells}×${size.cells}", maxLines = 1) }
                }
            }
            Text(
                "Changing the size starts a new board.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalFloodColors.current.muted,
            )

            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            TextButton(onClick = onHowToPlay) { Text("How to play") }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() },
    )
}

@Composable
private fun ThemeRow(theme: ColorTheme, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(theme.name, style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                theme.colors.forEach { color ->
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(color),
                    )
                }
            }
        }
    }
}

@Composable
private fun RulesDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Let's play") } },
        title = { Text("How to play") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Turn the whole board into a single color before you run out of moves.",
                    fontWeight = FontWeight.Medium,
                )
                listOf(
                    "Your flood starts in the top-left corner.",
                    "Tap a color below the board. The flood changes to that color and swallows every touching square that matches it.",
                    "Each tap is one move. Fill the board within the move limit to win.",
                    "Every board can be won within its limit.",
                ).forEachIndexed { i, step ->
                    Row {
                        Text("${i + 1}.", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(22.dp))
                        Text(step)
                    }
                }
                Text(
                    "Tip: go for the color that grabs the most squares, and push toward the far corner.",
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalFloodColors.current.muted,
                )
            }
        },
    )
}
