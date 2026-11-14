package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE9F0FD),
                        AppBackground,
                        Color(0xFFF5F2F8),
                    )
                )
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.06f),
                radius = size.minDimension * 0.38f,
                center = Offset(size.width * 0.12f, size.height * 0.12f),
            )
            drawCircle(
                color = OrangeAccent.copy(alpha = 0.06f),
                radius = size.minDimension * 0.30f,
                center = Offset(size.width * 0.90f, size.height * 0.25f),
            )
            drawCircle(
                color = PinkAccent.copy(alpha = 0.05f),
                radius = size.minDimension * 0.34f,
                center = Offset(size.width * 0.85f, size.height * 0.88f),
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.045f),
                radius = size.minDimension * 0.24f,
                center = Offset(size.width * 0.10f, size.height * 0.78f),
            )
        }
        content()
    }
}

@Composable
fun CoinPill(
    coins: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag("coin_balance_pill"),
        shape = RoundedCornerShape(50),
        color = OrangeSoft,
        shadowElevation = 2.dp,
        border = BorderStroke(1.2.dp, OrangeAccent.copy(alpha = 0.40f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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
            )
        }
    }
}

@Composable
fun ScorePill(
    score: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.testTag("score_pill"),
        shape = RoundedCornerShape(50),
        color = PrimaryBlueSoft,
        shadowElevation = 1.dp,
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
    isWarning: Boolean,
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

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (highlightColor != null) {
                    highlightColor.copy(alpha = 0.14f)
                } else {
                    Color(0xFFF6F8FC)
                },
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
