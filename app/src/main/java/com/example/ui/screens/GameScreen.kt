package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdManager
import com.example.model.GameUiState
import com.example.model.GridCoord
import com.example.model.PlayerProgress
import com.example.ui.components.BackCircleButton
import com.example.ui.components.CoinPill
import com.example.ui.components.GetHintOptionsDialog
import com.example.ui.components.MuteToggleButton
import com.example.ui.components.ScorePill
import com.example.ui.components.TargetWordsChips
import com.example.ui.components.TimerPill
import com.example.ui.components.WatchAdForCoinsDialog
import com.example.ui.components.WordGridBoard
import com.example.ui.components.WordlyAmbientBackground
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.HintYellowBorder
import com.example.ui.theme.MainText
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PinkSoft
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SurfaceWhite
import com.example.viewmodel.WordlyViewModel
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    gameState: GameUiState?,
    progress: PlayerProgress,
    onBack: () -> Unit,
    onToggleMute: () -> Unit,
    onSelectionChanged: (List<GridCoord>) -> Unit,
    onSelectionFinished: (List<GridCoord>) -> Unit,
    onUseHint: () -> Unit,
    onUseHintWithCoins: () -> Unit,
    onRewardedHintEarned: () -> Unit,
    onRewardedCoinsEarned: () -> Unit,
    onShowMessage: (String) -> Unit,
    onRoundFinishedNavigate: () -> Unit,
) {
    if (gameState == null) return

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var isSelectionActive by remember { mutableStateOf(false) }
    var livePreviewWord by remember(gameState.board) { mutableStateOf("") }
    var showCoinsDialog by remember { mutableStateOf(false) }
    var showHintOptionsDialog by remember { mutableStateOf(false) }

    val triggerWatchAdForCoins: () -> Unit = {
        AdManager.showRewardedAdForCoins(
            activity = activity,
            onRewardEarned = onRewardedCoinsEarned,
            onUnavailable = {
                onShowMessage("Ad unavailable right now. Try again shortly!")
            },
        )
    }

    val triggerWatchAdForHint: () -> Unit = {
        AdManager.showRewardedAdForHint(
            activity = activity,
            onRewardEarned = onRewardedHintEarned,
            onUnavailable = {
                onShowMessage("Ad unavailable right now. Try again shortly!")
            },
        )
    }

    if (showCoinsDialog) {
        WatchAdForCoinsDialog(
            currentCoins = progress.coins,
            onWatchAd = {
                showCoinsDialog = false
                triggerWatchAdForCoins()
            },
            onDismiss = { showCoinsDialog = false },
        )
    }

    if (showHintOptionsDialog) {
        GetHintOptionsDialog(
            currentCoins = progress.coins,
            coinCost = WordlyViewModel.COIN_HINT_COST,
            onWatchAdForHint = {
                showHintOptionsDialog = false
                triggerWatchAdForHint()
            },
            onUseCoinsForHint = {
                showHintOptionsDialog = false
                onUseHintWithCoins()
            },
            onDismiss = { showHintOptionsDialog = false },
        )
    }

    LaunchedEffect(gameState.category.id, gameState.isRoundFinished) {
        if (gameState.isRoundFinished) {
            delay(420L)
            if (!isSelectionActive) {
                onRoundFinishedNavigate()
            } else {
                while (isSelectionActive) {
                    delay(100L)
                }
                onRoundFinishedNavigate()
            }
        }
    }

    WordlyAmbientBackground(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 960.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                // Top Bar: Back, Level & Category Title, Coins, Mute
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BackCircleButton(onClick = onBack)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (gameState.isDailyChallenge) {
                                    "Daily ${gameState.dailyChallengeStage}/${GameUiState.DAILY_CHALLENGE_TOTAL_STAGES} • ${gameState.category.emoji} ${gameState.category.name}"
                                } else {
                                    "Level ${gameState.category.id} • ${gameState.category.emoji} ${gameState.category.name}"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 18.sp,
                                color = MainText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("game_level_title"),
                            )
                            Text(
                                text = "Find all ${gameState.board.placedWords.size} hidden words",
                                style = MaterialTheme.typography.labelMedium,
                                color = SecondaryText,
                                maxLines = 1,
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CoinPill(
                            coins = progress.coins,
                            onClick = { showCoinsDialog = true },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        MuteToggleButton(
                            isMuted = progress.isMuted,
                            onToggle = onToggleMute,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // HUD Row: Score, Words Found Progress, Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScorePill(score = gameState.score)

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = SurfaceWhite,
                        shadowElevation = 1.dp,
                        border = BorderStroke(1.dp, OutlineSubtle),
                    ) {
                        Text(
                            text = "${gameState.foundWords.size} / ${gameState.board.placedWords.size} words",
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("words_progress_text"),
                            style = MaterialTheme.typography.labelLarge,
                            color = SecondaryText,
                        )
                    }

                    TimerPill(seconds = gameState.elapsedSeconds)
                }

                Spacer(modifier = Modifier.height(8.dp))

                LiveStatusBanner(
                    liveWord = if (isSelectionActive) livePreviewWord else "",
                    bonusToastText = gameState.bonusToastText,
                    infoMessage = gameState.infoMessage,
                    bonusCount = gameState.bonusWordsFound.size,
                )

                // Main Play Area
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    val boxMaxWidth = maxWidth
                    val boxMaxHeight = maxHeight
                    val isWide = boxMaxWidth >= 660.dp

                    if (isWide) {
                        val maxGridSize = minOf(boxMaxHeight * 0.94f, boxMaxWidth * 0.54f).coerceAtMost(460.dp)
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1.15f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                WordGridBoard(
                                    board = gameState.board,
                                    foundWords = gameState.foundWords,
                                    hintedCells = gameState.hintedCells,
                                    hintPulseStep = gameState.hintPulseStep,
                                    wrongSelectionCells = gameState.wrongSelectionCells,
                                    isRoundFinished = gameState.isRoundFinished,
                                    onSelectionChanged = { cells, word ->
                                        livePreviewWord = word
                                        onSelectionChanged(cells)
                                    },
                                    onSelectionFinished = { cells ->
                                        livePreviewWord = ""
                                        onSelectionFinished(cells)
                                    },
                                    onSelectionActiveChanged = { active ->
                                        isSelectionActive = active
                                        if (!active) livePreviewWord = ""
                                    },
                                    modifier = Modifier.size(maxGridSize),
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                WordsCardPanel(gameState = gameState)
                                Spacer(modifier = Modifier.height(14.dp))
                                HintControlsRow(
                                    freeHintsRemaining = gameState.freeHintsRemaining,
                                    isRoundFinished = gameState.isRoundFinished,
                                    onUseFreeHint = onUseHint,
                                    onShowHintOptions = { showHintOptionsDialog = true },
                                )
                            }
                        }
                    } else {
                        val boardSide = minOf(boxMaxWidth - 8.dp, boxMaxHeight * 0.60f).coerceAtMost(420.dp)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                WordGridBoard(
                                    board = gameState.board,
                                    foundWords = gameState.foundWords,
                                    hintedCells = gameState.hintedCells,
                                    hintPulseStep = gameState.hintPulseStep,
                                    wrongSelectionCells = gameState.wrongSelectionCells,
                                    isRoundFinished = gameState.isRoundFinished,
                                    onSelectionChanged = { cells, word ->
                                        livePreviewWord = word
                                        onSelectionChanged(cells)
                                    },
                                    onSelectionFinished = { cells ->
                                        livePreviewWord = ""
                                        onSelectionFinished(cells)
                                    },
                                    onSelectionActiveChanged = { active ->
                                        isSelectionActive = active
                                        if (!active) livePreviewWord = ""
                                    },
                                    modifier = Modifier.size(boardSide),
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = (boxMaxHeight * 0.27f).coerceAtLeast(108.dp))
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.TopCenter,
                            ) {
                                WordsCardPanel(gameState = gameState)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            HintControlsRow(
                                freeHintsRemaining = gameState.freeHintsRemaining,
                                isRoundFinished = gameState.isRoundFinished,
                                onUseFreeHint = onUseHint,
                                onShowHintOptions = { showHintOptionsDialog = true },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveStatusBanner(
    liveWord: String,
    bonusToastText: String?,
    infoMessage: String?,
    bonusCount: Int,
) {
    val bannerState = when {
        bonusToastText != null -> "bonus" to bonusToastText
        infoMessage != null -> "info" to infoMessage
        liveWord.isNotEmpty() -> "drag" to liveWord
        else -> "idle" to if (bonusCount > 0) "Bonus words found: $bonusCount (+${bonusCount * 5} pts)" else "Drag across letter bubbles to find words"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = bannerState,
            transitionSpec = {
                (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.95f))
            },
            label = "live_status_banner",
        ) { (type, text) ->
            when (type) {
                "bonus" -> {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PinkAccent,
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                            )
                        }
                    }
                }

                "info" -> {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MainText.copy(alpha = 0.90f),
                        shadowElevation = 3.dp,
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                        )
                    }
                }

                "drag" -> {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PrimaryBlue,
                        shadowElevation = 4.dp,
                    ) {
                        Text(
                            text = text.toCharArray().joinToString(" "),
                            modifier = Modifier
                                .padding(horizontal = 18.dp, vertical = 5.dp)
                                .testTag("live_word_preview"),
                            fontFamily = FredokaFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                        )
                    }
                }

                else -> {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 12.5.sp,
                        color = if (bonusCount > 0) PinkAccent else SecondaryText,
                        fontWeight = if (bonusCount > 0) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun WordsCardPanel(gameState: GameUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = SurfaceWhite,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, OutlineSubtle),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (gameState.bonusWordsFound.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = PinkSoft,
                    ) {
                        Text(
                            text = "Bonus words: ${gameState.bonusWordsFound.size} (+${gameState.bonusWordPoints} pts)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = PinkAccent,
                        )
                    }
                }
            }

            TargetWordsChips(
                placedWords = gameState.board.placedWords,
                foundWords = gameState.foundWords,
            )
        }
    }
}

@Composable
private fun HintControlsRow(
    freeHintsRemaining: Int,
    isRoundFinished: Boolean,
    onUseFreeHint: () -> Unit,
    onShowHintOptions: () -> Unit,
) {
    val hasFreeHints = freeHintsRemaining > 0
    val isHintButtonEnabled = !isRoundFinished

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        // Single centered Hint button: shows remaining count when > 0; shows Ad symbol when 0 and opens 2-option Hint dialog
        Button(
            onClick = {
                if (hasFreeHints) onUseFreeHint() else onShowHintOptions()
            },
            enabled = isHintButtonEnabled,
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 164.dp)
                .testTag("hint_button"),
            shape = RoundedCornerShape(50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isHintButtonEnabled) 4.dp else 0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeSoft,
                contentColor = MainText,
                disabledContainerColor = Color(0xFFE6E8EE),
                disabledContentColor = SecondaryText,
            ),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isHintButtonEnabled) HintYellowBorder else OutlineSubtle,
            ),
        ) {
            Icon(
                imageVector = if (hasFreeHints) Icons.Rounded.Lightbulb else Icons.Rounded.OndemandVideo,
                contentDescription = if (hasFreeHints) {
                    stringResource(R.string.hint_label)
                } else {
                    "Watch ad or use 300 coins for hint"
                },
                tint = if (!isHintButtonEnabled) {
                    SecondaryText
                } else if (hasFreeHints) {
                    OrangeAccent
                } else {
                    PrimaryBlue
                },
                modifier = Modifier.size(19.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasFreeHints) stringResource(R.string.hint_label) else "Hint",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 14.sp,
                maxLines = 1,
                softWrap = false,
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (hasFreeHints) {
                Surface(
                    shape = CircleShape,
                    color = if (isHintButtonEnabled) OrangeAccent else SecondaryText.copy(alpha = 0.25f),
                ) {
                    Text(
                        text = freeHintsRemaining.toString(),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHintButtonEnabled) Color.White else SecondaryText,
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isHintButtonEnabled) PrimaryBlue else SecondaryText.copy(alpha = 0.25f),
                    modifier = Modifier.testTag("hint_ad_badge"),
                ) {
                    Text(
                        text = "AD",
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isHintButtonEnabled) Color.White else SecondaryText,
                    )
                }
            }
        }
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
