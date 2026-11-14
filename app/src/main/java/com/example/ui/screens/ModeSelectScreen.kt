package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.CategoriesRepository
import com.example.model.GameMode
import com.example.model.PlayerProgress
import com.example.ui.components.BackCircleButton
import com.example.ui.components.CoinPill
import com.example.ui.components.WordlyAmbientBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeSoft
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueSoft
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SurfaceWhite

@Composable
fun ModeSelectScreen(
    categoryId: Int,
    progress: PlayerProgress,
    onBack: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
) {
    val category = CategoriesRepository.getById(categoryId)

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
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BackCircleButton(onClick = onBack)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = stringResource(R.string.mode_select_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MainText,
                        )
                    }
                    CoinPill(coins = progress.coins)
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Selected category summary card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    color = SurfaceWhite,
                    border = BorderStroke(1.dp, OutlineSubtle),
                    shadowElevation = 4.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryBlueSoft,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = category.emoji, fontSize = 32.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Category #${category.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryBlue,
                            )
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MainText,
                            )
                            Text(
                                text = "8–10 hidden words • +50 coins on win",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SecondaryText,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ModeOptionCard(
                    title = stringResource(R.string.classic_mode),
                    subtitle = stringResource(R.string.classic_mode_desc),
                    badge = "Relaxed Pace",
                    icon = Icons.Rounded.AllInclusive,
                    accentColor = PrimaryBlue,
                    accentSoft = PrimaryBlueSoft,
                    testTag = "classic_mode_button",
                    onClick = { onModeSelected(GameMode.CLASSIC) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                ModeOptionCard(
                    title = stringResource(R.string.time_mode),
                    subtitle = stringResource(R.string.time_mode_desc),
                    badge = "2× Speed Bonus",
                    icon = Icons.Rounded.Timer,
                    accentColor = OrangeAccent,
                    accentSoft = OrangeSoft,
                    testTag = "time_mode_button",
                    onClick = { onModeSelected(GameMode.TIME) },
                )
            }
        }
    }
}

@Composable
private fun ModeOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    accentSoft: Color,
    testTag: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(26.dp),
        color = SurfaceWhite,
        shadowElevation = 5.dp,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = accentSoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(30.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accentSoft,
                ) {
                    Text(
                        text = badge,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MainText,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
