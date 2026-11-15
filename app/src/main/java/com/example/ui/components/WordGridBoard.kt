package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FoundWordEntry
import com.example.model.GridCoord
import com.example.model.PuzzleBoard
import com.example.ui.theme.HintYellow
import com.example.ui.theme.HintYellowBorder
import com.example.ui.theme.MainText
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SurfaceTileCreamBottom
import com.example.ui.theme.SurfaceTileCreamTop
import com.example.ui.theme.TileEdgeShadow
import com.example.ui.theme.WarningRed
import com.example.ui.theme.WarningRedSoft
import com.example.ui.theme.WoodBorder
import com.example.ui.theme.WoodDark
import com.example.ui.theme.WoodLight
import com.example.ui.theme.WoodMid
import com.example.ui.theme.WordHighlightColors
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

@Composable
fun WordGridBoard(
    board: PuzzleBoard,
    foundWords: List<FoundWordEntry>,
    hintedCells: Set<GridCoord>,
    hintPulseStep: Int,
    wrongSelectionCells: Set<GridCoord>,
    isRoundFinished: Boolean,
    isVibrationEnabled: Boolean = true,
    onSelectionChanged: (List<GridCoord>, String) -> Unit,
    onSelectionFinished: (List<GridCoord>) -> Unit,
    onSelectionActiveChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var activeSelection by remember(board) { mutableStateOf<List<GridCoord>>(emptyList()) }

    val foundCellColorMap = remember(foundWords) {
        buildMap {
            for (entry in foundWords) {
                for (coord in entry.cells) {
                    put(coord, entry.colorIndex)
                }
            }
        }
    }

    val activeSelectionSet = remember(activeSelection) { activeSelection.toSet() }

    fun wordFor(coords: List<GridCoord>): String = buildString(coords.size) {
        for (c in coords) {
            append(board.grid[c.row][c.col])
        }
    }

    // Soft pastel sky-blue & frosted-glass framed 8x8 puzzle board harmonizing with background
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = PrimaryBlue.copy(alpha = 0.22f),
                spotColor = PrimaryBlue.copy(alpha = 0.24f),
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(WoodLight, WoodMid, WoodDark)
                )
            )
            .border(
                width = 1.6.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.92f),
                        WoodBorder,
                    )
                ),
                shape = RoundedCornerShape(26.dp),
            )
            .padding(6.dp)
            .testTag("word_grid_board"),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF3F8FF),
            border = BorderStroke(1.dp, Color(0xFFD4E3FA)),
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
            ) {
                val boardSizePx = with(density) { maxWidth.toPx() }
                val gridSize = board.size
                val cellSizePx = boardSizePx / gridSize.toFloat()
                val letterFontSize = remember(maxWidth) {
                    (maxWidth.value / 14.2f).coerceIn(15f, 28f).sp
                }

                // Subtle connector dots between adjacent active bubbles without a straight-line overlay
                if (activeSelection.size >= 2) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0 until activeSelection.size - 1) {
                            val a = activeSelection[i]
                            val b = activeSelection[i + 1]
                            val ax = (a.col + 0.5f) * cellSizePx
                            val ay = (a.row + 0.5f) * cellSizePx
                            val bx = (b.col + 0.5f) * cellSizePx
                            val by = (b.row + 0.5f) * cellSizePx
                            val mid = Offset((ax + bx) / 2f, (ay + by) / 2f)
                            drawCircle(
                                color = PrimaryBlue.copy(alpha = 0.50f),
                                radius = cellSizePx * 0.12f,
                                center = mid,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(board, isRoundFinished, isVibrationEnabled) {
                            if (isRoundFinished) return@pointerInput
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                onSelectionActiveChanged(true)

                                val startCol = (down.position.x / cellSizePx).toInt().coerceIn(0, gridSize - 1)
                                val startRow = (down.position.y / cellSizePx).toInt().coerceIn(0, gridSize - 1)
                                val startCell = GridCoord(startRow, startCol)
                                var currentSelection = listOf(startCell)
                                activeSelection = currentSelection
                                onSelectionChanged(currentSelection, wordFor(currentSelection))

                                val startCenterX = (startCol + 0.5f) * cellSizePx
                                val startCenterY = (startRow + 0.5f) * cellSizePx

                                var pointerDown = true
                                while (pointerDown) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                        ?: event.changes.firstOrNull()
                                    if (change == null || !change.pressed) {
                                        pointerDown = false
                                        change?.consume()
                                        break
                                    }
                                    change.consume()
                                    val next = computeRaySelection(
                                        start = startCell,
                                        startCenterX = startCenterX,
                                        startCenterY = startCenterY,
                                        pointerX = change.position.x,
                                        pointerY = change.position.y,
                                        cellSizePx = cellSizePx,
                                        gridSize = gridSize,
                                    )
                                    if (next != currentSelection) {
                                        currentSelection = next
                                        activeSelection = next
                                        if (isVibrationEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        onSelectionChanged(next, wordFor(next))
                                    }
                                }

                                val finalSelection = currentSelection
                                activeSelection = emptyList()
                                onSelectionActiveChanged(false)
                                onSelectionFinished(finalSelection)
                            }
                        },
                ) {
                    for (row in 0 until gridSize) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            for (col in 0 until gridSize) {
                                val coord = GridCoord(row, col)
                                val letter = board.grid[row][col]
                                val isSelected = coord in activeSelectionSet
                                val foundColorIndex = foundCellColorMap[coord]
                                val isHinted = coord in hintedCells
                                val isWrong = coord in wrongSelectionCells

                                CreamBubbleTileCell(
                                    letter = letter,
                                    isSelected = isSelected,
                                    foundColorIndex = foundColorIndex,
                                    isHinted = isHinted,
                                    hintPulseOn = isHinted && (hintPulseStep % 2 == 1),
                                    isWrong = isWrong,
                                    fontSize = letterFontSize,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(1.5.dp)
                                        .testTag("grid_cell_${row}_${col}"),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreamBubbleTileCell(
    letter: Char,
    isSelected: Boolean,
    foundColorIndex: Int?,
    isHinted: Boolean,
    hintPulseOn: Boolean,
    isWrong: Boolean,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val highlightColor = foundColorIndex?.let {
        WordHighlightColors[it % WordHighlightColors.size]
    }

    val targetScale = when {
        isSelected -> 1.10f
        isHinted && hintPulseOn -> 1.10f
        isWrong -> 0.93f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "tile_scale",
    )

    val tileOverlayColor by animateColorAsState(
        targetValue = when {
            isSelected -> PrimaryBlue
            isWrong -> WarningRedSoft
            isHinted && hintPulseOn -> HintYellow.copy(alpha = 0.85f)
            isHinted -> HintYellow.copy(alpha = 0.40f)
            highlightColor != null -> highlightColor.copy(alpha = 0.35f)
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 150),
        label = "tile_overlay",
    )

    val tileBorderColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color(0xFF1D4ED8)
            isWrong -> WarningRed
            isHinted -> HintYellowBorder
            highlightColor != null -> highlightColor.copy(alpha = 0.78f)
            else -> Color(0xFFD3E2F8)
        },
        animationSpec = tween(durationMillis = 150),
        label = "tile_border",
    )

    val letterColor by animateColorAsState(
        targetValue = when {
            isSelected -> Color.White
            isWrong -> WarningRed
            else -> MainText
        },
        animationSpec = tween(durationMillis = 130),
        label = "tile_letter",
    )

    // Crisp sky-white bevelled tile base, transitioning to circular bubble when selected
    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .then(
                if (isSelected) {
                    Modifier.shadow(elevation = 6.dp, shape = CircleShape, clip = false)
                } else {
                    Modifier
                }
            )
            .clip(if (isSelected) CircleShape else RoundedCornerShape(10.dp))
            .background(TileEdgeShadow)
            .padding(bottom = if (isSelected) 0.dp else 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(if (isSelected) CircleShape else RoundedCornerShape(9.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SurfaceTileCreamTop, SurfaceTileCreamBottom)
                    )
                )
                .then(
                    if (tileOverlayColor != Color.Transparent) {
                        Modifier.background(tileOverlayColor)
                    } else {
                        Modifier
                    }
                )
                .border(
                    width = if (isSelected || highlightColor != null || isHinted || isWrong) 1.8.dp else 1.dp,
                    color = tileBorderColor,
                    shape = if (isSelected) CircleShape else RoundedCornerShape(9.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = letter.toString(),
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                color = letterColor,
            )
        }
    }
}

private fun computeRaySelection(
    start: GridCoord,
    startCenterX: Float,
    startCenterY: Float,
    pointerX: Float,
    pointerY: Float,
    cellSizePx: Float,
    gridSize: Int,
): List<GridCoord> {
    val dx = pointerX - startCenterX
    val dy = pointerY - startCenterY
    val dist = hypot(dx, dy)
    if (dist < cellSizePx * 0.42f) {
        return listOf(start)
    }

    val angle = atan2(dy.toDouble(), dx.toDouble())
    val octant = (angle / (PI / 4.0)).roundToInt()

    val (stepR, stepC) = when (octant) {
        0 -> 0 to 1
        1 -> 1 to 1
        2 -> 1 to 0
        3 -> 1 to -1
        4, -4 -> 0 to -1
        -3 -> -1 to -1
        -2 -> -1 to 0
        -1 -> -1 to 1
        else -> 0 to 1
    }

    val isDiagonal = stepR != 0 && stepC != 0
    val unitLen = if (isDiagonal) cellSizePx * 1.4142135f else cellSizePx
    val rayLen = hypot(stepC.toFloat(), stepR.toFloat())
    val ux = stepC / rayLen
    val uy = stepR / rayLen

    val projection = (dx * ux + dy * uy).coerceAtLeast(0f)
    val rawSteps = (projection / unitLen).roundToInt()

    val maxRowSteps = when {
        stepR > 0 -> (gridSize - 1) - start.row
        stepR < 0 -> start.row
        else -> gridSize - 1
    }
    val maxColSteps = when {
        stepC > 0 -> (gridSize - 1) - start.col
        stepC < 0 -> start.col
        else -> gridSize - 1
    }
    val clampedSteps = rawSteps.coerceIn(0, minOf(maxRowSteps, maxColSteps))

    return List(clampedSteps + 1) { step ->
        GridCoord(
            row = start.row + stepR * step,
            col = start.col + stepC * step,
        )
    }
}
