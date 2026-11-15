package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.FoundWordEntry
import com.example.model.PlacedWord
import com.example.ui.theme.AppBackground
import com.example.ui.theme.FoundWordText
import com.example.ui.theme.MainText
import com.example.ui.theme.NunitoFontFamily
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PinkAccent
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueSoft
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.WarningRed
import com.example.ui.theme.WarningRedSoft
import com.example.ui.theme.WordHighlightColors
import kotlinx.coroutines.delay

fun formatSecondsMMSS(totalSeconds: Int): String {
    val clamped = totalSeconds.coerceAtLeast(0)
    val mins = clamped / 60
    val secs = clamped % 60
    return "%02d:%02d".format(mins, secs)
}

@Composable
fun WordlyAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_orbs")
    val drift by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient_drift",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3EEFF),
                        AppBackground,
                        Color(0xFFF6F0FA),
                    )
                )
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.08f),
                radius = size.minDimension * 0.36f,
                center = Offset(size.width * (0.12f + 0.03f * drift), size.height * (0.12f - 0.02f * drift)),
            )
            drawCircle(
                color = OrangeAccent.copy(alpha = 0.075f),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * (0.90f - 0.03f * drift), size.height * (0.24f + 0.03f * drift)),
            )
            drawCircle(
                color = PinkAccent.copy(alpha = 0.06f),
                radius = size.minDimension * 0.32f,
                center = Offset(size.width * (0.84f + 0.025f * drift), size.height * (0.86f - 0.025f * drift)),
            )
            drawCircle(
                color = Color(0xFF06B6D4).copy(alpha = 0.055f),
                radius = size.minDimension * 0.22f,
                center = Offset(size.width * (0.11f - 0.02f * drift), size.height * (0.76f + 0.03f * drift)),
            )
        }
        content()
    }
}

@Composable
fun CoinPill(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val showPlusBadge = onClick != null
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.MonetizationOn,
                contentDescription = "Coins",
                tint = OrangeAccent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = coins.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MainText,
                maxLines = 1,
                softWrap = false,
            )
            if (showPlusBadge) {
                Spacer(modifier = Modifier.width(5.dp))
                Icon(
                    imageVector = Icons.Rounded.AddCircle,
                    contentDescription = "Watch ad for 500 coins",
                    tint = PrimaryBlue,
                    modifier = Modifier
                        .size(18.dp)
                        .testTag("coin_plus_icon"),
                )
            }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier.testTag("coin_balance_pill"),
            shape = RoundedCornerShape(50),
            color = OrangeSoft,
            shadowElevation = 2.dp,
            border = BorderStroke(1.2.dp, OrangeAccent.copy(alpha = 0.45f)),
            content = content,
        )
    } else {
        Surface(
            modifier = modifier.testTag("coin_balance_pill"),
            shape = RoundedCornerShape(50),
            color = OrangeSoft,
            shadowElevation = 2.dp,
            border = BorderStroke(1.2.dp, OrangeAccent.copy(alpha = 0.40f)),
            content = content,
        )
    }
}

@Composable
fun WatchAdForCoinsDialog(
    currentCoins: Int,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.MonetizationOn,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get +500 Coins",
                    style = MaterialTheme.typography.titleLarge,
                    color = MainText,
                )
            }
        },
        text = {
            Text(
                text = "Current balance: $currentCoins coins.\n\nWatch a short rewarded ad to get +500 free coins!",
                style = MaterialTheme.typography.bodyMedium,
                color = MainText,
            )
        },
        confirmButton = {
            Button(
                onClick = onWatchAd,
                modifier = Modifier.testTag("watch_ad_500_dialog_button"),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Watch Ad (+500)",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Close", color = PrimaryBlue)
            }
        },
    )
}

@Composable
fun GetHintOptionsDialog(
    currentCoins: Int,
    coinCost: Int = 300,
    onWatchAdForHint: () -> Unit,
    onUseCoinsForHint: () -> Unit,
    onDismiss: () -> Unit,
) {
    val hasEnoughCoins = currentCoins >= coinCost
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Get a Hint",
                    style = MaterialTheme.typography.titleLarge,
                    color = MainText,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Free hints finished! Choose how to unlock your next hint (Balance: $currentCoins coins):",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MainText,
                )

                Button(
                    onClick = onWatchAdForHint,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_hint_watch_ad_button"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Watch Ad • Get Hint",
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                OutlinedButton(
                    onClick = onUseCoinsForHint,
                    enabled = hasEnoughCoins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dialog_hint_300_coins_button"),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        1.4.dp,
                        if (hasEnoughCoins) OrangeAccent else OutlineSubtle,
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (hasEnoughCoins) OrangeSoft else Color(0xFFF3F4F6),
                        contentColor = MainText,
                        disabledContentColor = MainText.copy(alpha = 0.45f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MonetizationOn,
                        contentDescription = null,
                        tint = if (hasEnoughCoins) OrangeAccent else MainText.copy(alpha = 0.45f),
                        modifier = Modifier.size(19.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hasEnoughCoins) {
                            "Use $coinCost Coins"
                        } else {
                            "Use $coinCost Coins (Need ${coinCost - currentCoins} more)"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel", color = PrimaryBlue)
            }
        },
    )
}

@Composable
fun ScorePill(
    score: Int,
    modifier: Modifier = Modifier,
) {
    var bumped by remember { mutableStateOf(false) }
    LaunchedEffect(score) {
        if (score > 0) {
            bumped = true
            delay(180L)
            bumped = false
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (bumped) 1.09f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "score_pop",
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("score_pill"),
        shape = RoundedCornerShape(50),
        color = PrimaryBlueSoft,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.30f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Stars,
                contentDescription = "Score",
                tint = PrimaryBlue,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = "$score pts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlue,
            )
        }
    }
}

@Composable
fun TimerPill(
    seconds: Int,
    isWarning: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val bgColor = if (isWarning) WarningRedSoft else SurfaceWhite
    val borderColor = if (isWarning) WarningRed else OutlineSubtle
    val contentColor = if (isWarning) WarningRed else MainText

    Surface(
        modifier = modifier.testTag("timer_pill"),
        shape = RoundedCornerShape(50),
        color = bgColor,
        shadowElevation = 1.dp,
        border = BorderStroke(1.2.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = "Timer",
                tint = contentColor,
                modifier = Modifier.size(17.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = formatSecondsMMSS(seconds),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor,
            )
        }
    }
}

@Composable
fun MuteToggleButton(
    isMuted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("mute_toggle_button"),
        shape = CircleShape,
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, OutlineSubtle),
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                imageVector = if (isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                contentDescription = stringResource(if (isMuted) R.string.sound_off else R.string.sound_on),
                tint = if (isMuted) FoundWordText else PrimaryBlue,
            )
        }
    }
}

@Composable
fun BackCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("back_button"),
        shape = CircleShape,
        color = SurfaceWhite,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, OutlineSubtle),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back_label),
                tint = MainText,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TargetWordsChips(
    placedWords: List<PlacedWord>,
    foundWords: List<FoundWordEntry>,
    modifier: Modifier = Modifier,
) {
    val foundByWord = foundWords.associateBy { it.word }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("target_words_list"),
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        for (placed in placedWords) {
            val found = foundByWord[placed.word]
            val highlightColor = found?.let {
                WordHighlightColors[it.colorIndex % WordHighlightColors.size]
            }
            var chipPop by remember(placed.word) { mutableStateOf(false) }
            LaunchedEffect(found != null) {
                if (found != null) {
                    chipPop = true
                    delay(200L)
                    chipPop = false
                }
            }
            val chipScale by animateFloatAsState(
                targetValue = if (chipPop) 1.12f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
                label = "chip_pop_${placed.word}",
            )
            val chipBg by animateColorAsState(
                targetValue = highlightColor?.copy(alpha = 0.14f) ?: Color(0xFFF6F8FC),
                animationSpec = tween(durationMillis = 220),
                label = "chip_bg_${placed.word}",
            )

            Surface(
                modifier = Modifier.graphicsLayer {
                    scaleX = chipScale
                    scaleY = chipScale
                },
                shape = RoundedCornerShape(14.dp),
                color = chipBg,
                border = BorderStroke(
                    width = 1.dp,
                    color = highlightColor?.copy(alpha = 0.45f) ?: OutlineSubtle,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (highlightColor != null) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = highlightColor,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = placed.word,
                        fontFamily = NunitoFontFamily,
                        fontWeight = if (found != null) FontWeight.SemiBold else FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = if (found != null) FoundWordText else MainText,
                        textDecoration = if (found != null) TextDecoration.LineThrough else TextDecoration.None,
                    )
                }
            }
        }
    }
}
