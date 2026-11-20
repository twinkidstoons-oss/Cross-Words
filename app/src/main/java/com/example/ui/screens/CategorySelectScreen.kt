package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ads.AdManager
import com.example.data.CategoriesRepository
import com.example.model.Category
import com.example.model.PlayerProgress
import com.example.ui.components.BackCircleButton
import com.example.ui.components.CoinPill
import com.example.ui.components.WatchAdForCoinsDialog
import com.example.ui.components.WordlyAmbientBackground
import com.example.ui.theme.MainText
import com.example.ui.theme.OutlineSubtle
import com.example.ui.theme.SecondaryText
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenSoft
import com.example.ui.theme.SurfaceSoft
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.WordHighlightColors
import kotlinx.coroutines.launch

@Composable
fun CategorySelectScreen(
    progress: PlayerProgress,
    onBack: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    onRewardedCoinsEarned: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val categories = CategoriesRepository.categories
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
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
                        .widthIn(max = 720.dp),
                ) {
                    // Header: left title column gets weight(1f) so CoinPill never clips or wraps
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            BackCircleButton(onClick = onBack)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.categories_title),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MainText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "Complete a level to unlock the next",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SecondaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        CoinPill(
                            coins = progress.coins,
                            onClick = { showCoinsDialog = true },
                        )
                    }

                    // 25 Categories Grid in exact order
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 156.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("categories_grid"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(categories, key = { it.id }) { category ->
                            val isUnlocked = progress.isCategoryUnlocked(category.id)
                            val isCompleted = progress.isCategoryCompleted(category.id)
                            val accent = WordHighlightColors[(category.id - 1) % WordHighlightColors.size]

                            CategoryCard(
                                category = category,
                                accentColor = accent,
                                isUnlocked = isUnlocked,
                                isCompleted = isCompleted,
                                onClick = {
                                    if (isUnlocked) {
                                        onCategorySelected(category)
                                    } else {
                                        val prevName = CategoriesRepository.getById(category.id - 1).name
                                        scope.launch {
                                            snackbarHostState.currentSnackbarData?.dismiss()
                                            snackbarHostState.showSnackbar("Complete $prevName to unlock ${category.name}!")
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    accentColor: Color,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    var visible by remember(category.id) { mutableStateOf(false) }
    LaunchedEffect(category.id) {
        visible = true
    }
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cat_scale_${category.id}",
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "cat_alpha_${category.id}",
    )

    val cardBg = if (isUnlocked) SurfaceWhite else SurfaceSoft
    val borderColor = when {
        isCompleted -> SuccessGreen.copy(alpha = 0.55f)
        isUnlocked -> accentColor.copy(alpha = 0.35f)
        else -> OutlineSubtle
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
            .testTag("category_card_${category.id}"),
        shape = RoundedCornerShape(24.dp),
        color = cardBg,
        shadowElevation = if (isUnlocked) 4.dp else 0.dp,
        border = BorderStroke(1.3.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isUnlocked) accentColor.copy(alpha = 0.14f) else Color(0xFFE9ECF2),
                ) {
                    Text(
                        text = "#${category.id}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isUnlocked) accentColor else SecondaryText,
                    )
                }

                when {
                    isCompleted -> {
                        Surface(
                            shape = CircleShape,
                            color = SuccessGreenSoft,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Completed",
                                tint = SuccessGreen,
                                modifier = Modifier
                                    .padding(3.dp)
                                    .size(18.dp),
                            )
                        }
                    }

                    !isUnlocked -> {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE9ECF2),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Locked",
                                tint = SecondaryText,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(16.dp),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isUnlocked) accentColor.copy(alpha = 0.12f) else Color(0xFFECEEF3),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = category.emoji,
                        fontSize = 26.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUnlocked) MainText else SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = if (isUnlocked) "${category.words.size} words" else "Locked",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 12.sp,
                color = SecondaryText,
            )
        }
    }
}
