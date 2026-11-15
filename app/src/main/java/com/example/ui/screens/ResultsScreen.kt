package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdManager
import com.example.model.PlayerProgress
import com.example.model.RoundResult
import com.example.ui.components.CoinPill
import com.example.ui.components.WatchAdForCoinsDialog
import com.example.ui.components.WordlyAmbientBackground
import com.example.ui.components.formatSecondsMMSS
import com.example.ui.theme.MainText
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueSoft
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.WarningRed
import com.example.ui.theme.WarningRedSoft
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ResultsScreen(
    result: RoundResult?,
    progress: PlayerProgress,
    onPlayAgain: (Int) -> Unit,
    onNextLevel: (Int) -> Unit,
    onHome: () -> Unit,
    onRewardedCoinsEarned: () -> Unit = {},
) {
    BackHandler { onHome() }

    if (result == null) {
        WordlyAmbientBackground(
            modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        ) {}
        return
    }

    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCoinsDialog by remember { mutableStateOf(false) }

    var animateEntry by remember(result) { mutableStateOf(false) }
    LaunchedEffect(result) {
        animateEntry = true
    }

    val badgeScale by animateFloatAsState(
        targetValue = if (animateEntry) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "results_badge_scale",
    )

    val animatedScore by animateIntAsState(
        targetValue = if (animateEntry) result.totalScore else 0,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "results_score_countup",
    )

    val animatedCoinsEarned by animateIntAsState(
        targetValue = if (animateEntry) result.coinsEarned else 0,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "results_coins_countup",
    )

    val celebrationTransition = rememberInfiniteTransition(label = "results_confetti")
    val confettiPhase by celebrationTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "confetti_phase",
    )

    if (showCoinsDialog) {
        WatchAdForCoinsDialog(
            currentCoins = progress.coins,
            onWatchAd = {
                showCoinsDialog = false
                AdManager.showRewardedAdForCoins(
                    activity = activity,
                    onRewardEarned = {
                        onRewardedCoinsEarned()
                        scope.launch {
                            snackbarHostState.showSnackbar("+500 Coins added!")
                        }
                    },
                    onUnavailable = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Ad unavailable right now. Try again shortly!")
                        }
                    },
                )
            },
            onDismiss = { showCoinsDialog = false },
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        WordlyAmbientBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 560.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        CoinPill(
                            coins = progress.coins,
                            onClick = { showCoinsDialog = true },
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Hero Status Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = SurfaceWhite,
                        shadowElevation = 7.dp,
                        border = BorderStroke(1.dp, OutlineSubtle),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .graphicsLayer {
                                        scaleX = badgeScale
                                        scaleY = badgeScale
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (result.completed) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val dots = listOf(
                                            Triple(0.14f, 0.22f, OrangeAccent),
                                            Triple(0.87f, 0.20f, PinkAccent),
                                            Triple(0.11f, 0.74f, PrimaryBlue),
                                            Triple(0.90f, 0.76f, SuccessGreen),
                                            Triple(0.50f, 0.06f, Color(0xFFFACC15)),
                                            Triple(0.32f, 0.90f, PinkAccent),
                                            Triple(0.70f, 0.90f, OrangeAccent),
                                        )
                                        dots.forEachIndexed { index, (x, y, c) ->
                                            val driftX = cos(confettiPhase + index * 0.9f) * 3.dp.toPx()
                                            val driftY = sin(confettiPhase + index * 1.1f) * 3.dp.toPx()
                                            val radiusPulse = 4.2.dp.toPx() + 1.2.dp.toPx() * sin(confettiPhase + index)
                                            drawCircle(
                                                color = c.copy(alpha = 0.88f),
                                                radius = radiusPulse,
                                                center = Offset(size.width * x + driftX, size.height * y + driftY),
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier.size(72.dp),
                                    shape = CircleShape,
                                    color = if (result.completed) SuccessGreenSoft else WarningRedSoft,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = if (result.completed) {
                                                Icons.Rounded.EmojiEvents
                                            } else {
                                                Icons.Rounded.HourglassBottom
                                            },
                                            contentDescription = null,
                                            tint = if (result.completed) SuccessGreen else WarningRed,
                                            modifier = Modifier.size(38.dp),
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Level Complete!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MainText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("results_title"),
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Level ${result.categoryId} • ${result.categoryEmoji} ${result.categoryName}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SecondaryText,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Highlighted Total Score & Coins Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    color = PrimaryBlueSoft,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "Total Score",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = PrimaryBlue,
                                        )
                                        Text(
                                            text = animatedScore.toString(),
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = PrimaryBlue,
                                            modifier = Modifier.testTag("results_total_score"),
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(20.dp),
                                    color = OrangeSoft,
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            text = "Coins Earned",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = OrangeAccent,
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.MonetizationOn,
                                                contentDescription = null,
                                                tint = OrangeAccent,
                                                modifier = Modifier.size(22.dp),
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "+$animatedCoinsEarned",
                                                style = MaterialTheme.typography.headlineLarge,
                                                color = OrangeAccent,
                                                modifier = Modifier.testTag("results_coins_earned"),
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = OutlineSubtle)
                            Spacer(modifier = Modifier.height(14.dp))

                            ResultStatRow(
                                label = "Words Found",
                                value = "${result.wordsFoundCount} / ${result.totalWordsCount}",
                            )
                            ResultStatRow(
                                label = "Word Points (+10)",
                                value = "+${result.wordPoints}",
                            )
                            ResultStatRow(
                                label = "Bonus Words (${result.bonusWordsCount} × 5)",
                                value = "+${result.bonusWordPoints}",
                                valueColor = if (result.bonusWordPoints > 0) PinkAccent else MainText,
                            )
                            ResultStatRow(
                                label = "Elapsed Time",
                                value = formatSecondsMMSS(result.elapsedSeconds),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    if (result.completed && result.hasNextLevel) {
                        Button(
                            onClick = {
                                AdManager.maybeShowInterstitial(activity = activity) {
                                    onNextLevel(result.categoryId + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("next_level_button"),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(PrimaryBlue, PrimaryBlueDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Next: Level ${result.categoryId + 1}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontSize = 18.sp,
                                        maxLines = 1,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null,
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                AdManager.maybeShowInterstitial(activity = activity) {
                                    onPlayAgain(result.categoryId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("play_again_button"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.4.dp, PrimaryBlue),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceWhite,
                                contentColor = PrimaryBlue,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.play_again),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        OutlinedButton(
                            onClick = onHome,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("home_button"),
                            shape = RoundedCornerShape(18.dp),
                            border = BorderStroke(1.2.dp, OutlineSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = SurfaceWhite,
                                contentColor = MainText,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Home,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.home_label),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ResultStatRow(
    label: String,
    value: String,
    valueColor: Color = MainText,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SecondaryText,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor,
        )
    }
}
