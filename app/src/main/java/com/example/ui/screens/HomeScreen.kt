package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.data.CategoriesRepository
import com.example.model.PlayerProgress
import com.example.ui.components.CoinPill
import com.example.ui.components.MuteToggleButton
import com.example.ui.components.WatchAdForCoinsDialog
import com.example.ui.components.WordlyAmbientBackground
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.MainText
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueDark
import com.example.ui.theme.PrimaryBlueSoft
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SurfaceWhite
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    progress: PlayerProgress,
    onPlayClick: () -> Unit,
    onToggleMute: () -> Unit,
    onRewardedCoinsEarned: () -> Unit,
) {
    val totalCategories = CategoriesRepository.categories.size
    val unlockedCount = CategoriesRepository.categories.count {
        progress.isCategoryUnlocked(it.id)
    }
    val completedCount = progress.completedCategoryIds.size
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showCoinsDialog by remember { mutableStateOf(false) }

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
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("+500 Coins added!")
                        }
                    },
                    onUnavailable = {
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            snackbarHostState.showSnackbar("Ad unavailable right now. Please try again shortly!")
                        }
                    },
                )
            },
            onDismiss = { showCoinsDialog = false },
        )
    }

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val heroScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "home_hero_scale",
    )

    val infiniteTransition = rememberInfiniteTransition(label = "home_animations")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "logo_bob",
    )
    val tileTilt by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "tile_tilt",
    )
    val playPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "play_pulse_scale",
    )
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "play_halo_scale",
    )
    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.36f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "play_halo_alpha",
    )
    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "play_shimmer",
    )
    val playArrowOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "play_arrow_nudge",
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (completedCount.toFloat() / totalCategories.toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "journey_progress",
    )

    val playInteractionSource = remember { MutableInteractionSource() }
    val isPlayPressed by playInteractionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPlayPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "play_press_scale",
    )

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
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top Utility Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CoinPill(
                        coins = progress.coins,
                        onClick = { showCoinsDialog = true },
                    )
                    MuteToggleButton(
                        isMuted = progress.isMuted,
                        onToggle = onToggleMute,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bubble Letter Logo Hero Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = heroScale
                            scaleY = heroScale
                        },
                    shape = RoundedCornerShape(30.dp),
                    color = SurfaceWhite,
                    shadowElevation = 9.dp,
                    border = BorderStroke(1.5.dp, PrimaryBlue.copy(alpha = 0.18f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val logoTiles = remember {
                            listOf(
                                'W' to PrimaryBlue,
                                'O' to OrangeAccent,
                                'R' to PinkAccent,
                                'D' to SuccessGreen,
                                'L' to Color(0xFF8B5CF6),
                                'Y' to Color(0xFF06B6D4),
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            logoTiles.forEachIndexed { idx, (ch, color) ->
                                val phase = if (idx % 2 == 0) floatOffset else -floatOffset
                                val angle = if (idx % 2 == 0) tileTilt else -tileTilt
                                Surface(
                                    modifier = Modifier
                                        .size(43.dp)
                                        .offset(y = phase.dp)
                                        .graphicsLayer { rotationZ = angle },
                                    shape = CircleShape,
                                    color = color,
                                    shadowElevation = 6.dp,
                                    border = BorderStroke(1.8.dp, Color.White.copy(alpha = 0.72f)),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = ch.toString(),
                                            fontFamily = FredokaFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 21.sp,
                                            color = Color.White,
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.displayLarge,
                            color = MainText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag("home_title"),
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Swipe & connect letter bubbles across 25 vibrant word puzzles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        ) {
                            FeatureMiniChip(
                                icon = Icons.Rounded.GridView,
                                label = "8×8 Grid",
                                tint = PrimaryBlue,
                            )
                            FeatureMiniChip(
                                icon = Icons.Rounded.AutoAwesome,
                                label = "25 Levels",
                                tint = PinkAccent,
                            )
                            FeatureMiniChip(
                                icon = Icons.Rounded.Lightbulb,
                                label = "Smart Hints",
                                tint = OrangeAccent,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progression Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = SurfaceWhite,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, OutlineSubtle),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Puzzle Journey",
                                style = MaterialTheme.typography.titleMedium,
                                color = MainText,
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = PrimaryBlueSoft,
                            ) {
                                Text(
                                    text = "$completedCount / $totalCategories Completed",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryBlue,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(50)),
                            color = PrimaryBlue,
                            trackColor = PrimaryBlueSoft,
                            strokeCap = StrokeCap.Round,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "$unlockedCount of $totalCategories levels unlocked",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SecondaryText,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Animated Pulsing + Shimmering Play Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    // Glowing pulse ring behind the Play button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .graphicsLayer {
                                scaleX = haloScale
                                scaleY = haloScale
                                alpha = haloAlpha
                            }
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(PrimaryBlue, Color(0xFF38BDF8), PrimaryBlueDark)
                                )
                            ),
                    )

                    Button(
                        onClick = onPlayClick,
                        interactionSource = playInteractionSource,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .graphicsLayer {
                                val combinedScale = playPulseScale * pressScale
                                scaleX = combinedScale
                                scaleY = combinedScale
                            }
                            .testTag("play_button"),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(PrimaryBlue, Color(0xFF2563EB), PrimaryBlueDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Moving diagonal shimmer highlight across button
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width * shimmerProgress
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.24f),
                                            Color.Transparent,
                                        ),
                                        start = Offset(centerX - size.width * 0.22f, 0f),
                                        end = Offset(centerX + size.width * 0.22f, size.height),
                                    )
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.22f),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .offset(x = playArrowOffset.dp),
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(R.string.play_now),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontSize = 21.sp,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun FeatureMiniChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.11f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MainText,
            )
        }
    }
}
