package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.AdManager
import com.example.ui.screens.CategorySelectScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.theme.WordlyPuzzleTheme
import com.example.viewmodel.WordlyViewModel

object WordlyRoutes {
    const val HOME = "home"
    const val CATEGORY_SELECT = "category_select"
    const val GAME = "game/{categoryId}"
    const val RESULTS = "results"

    fun game(categoryId: Int) = "game/$categoryId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AdManager.initialize(this)

        setContent {
            WordlyPuzzleTheme {
                WordlyPuzzleApp()
            }
        }
    }
}

@Composable
fun WordlyPuzzleApp(
    viewModel: WordlyViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val progress by viewModel.playerProgress.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val roundResult by viewModel.roundResult.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = WordlyRoutes.HOME,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth / 6 },
                animationSpec = tween(260),
            ) + fadeIn(animationSpec = tween(220))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 8 },
                animationSpec = tween(220),
            ) + fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 6 },
                animationSpec = tween(260),
            ) + fadeIn(animationSpec = tween(220))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth / 6 },
                animationSpec = tween(220),
            ) + fadeOut(animationSpec = tween(180))
        },
    ) {
        composable(WordlyRoutes.HOME) {
            HomeScreen(
                progress = progress,
                onPlayClick = {
                    viewModel.playTapSound()
                    navController.navigate(WordlyRoutes.CATEGORY_SELECT)
                },
                onToggleMute = { viewModel.toggleMute() },
                onRewardedCoinsEarned = { viewModel.grantRewardedCoins() },
            )
        }

        composable(WordlyRoutes.CATEGORY_SELECT) {
            CategorySelectScreen(
                progress = progress,
                onBack = {
                    viewModel.playTapSound()
                    navController.popBackStack()
                },
                onCategorySelected = { category ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = category.id)
                    navController.navigate(WordlyRoutes.game(category.id))
                },
                onRewardedCoinsEarned = { viewModel.grantRewardedCoins() },
            )
        }

        composable(
            route = WordlyRoutes.GAME,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val routeCategoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1

            LaunchedEffect(routeCategoryId) {
                val current = gameState
                if (current == null || (current.category.id != routeCategoryId && !current.isRoundFinished)) {
                    viewModel.startNewGame(categoryId = routeCategoryId)
                }
            }

            GameScreen(
                gameState = gameState,
                progress = progress,
                onBack = {
                    viewModel.playTapSound()
                    viewModel.pauseOrStopGame()
                    navController.popBackStack()
                },
                onToggleMute = { viewModel.toggleMute() },
                onSelectionChanged = { cells ->
                    if (cells.size >= 2) {
                        viewModel.playSwipeSound()
                    }
                },
                onSelectionFinished = { cells ->
                    viewModel.submitSelection(cells)
                },
                onUseHint = { viewModel.useHint() },
                onUseHintWithCoins = { viewModel.useHintWithCoins() },
                onRewardedHintEarned = { viewModel.grantAndUseRewardedHint() },
                onRewardedCoinsEarned = { viewModel.grantRewardedCoins() },
                onShowMessage = { msg -> viewModel.showTransientMessage(msg) },
                onRoundFinishedNavigate = {
                    navController.navigate(WordlyRoutes.RESULTS) {
                        launchSingleTop = true
                        popUpTo(WordlyRoutes.CATEGORY_SELECT) {
                            inclusive = false
                        }
                    }
                },
            )
        }

        composable(WordlyRoutes.RESULTS) {
            ResultsScreen(
                result = roundResult,
                progress = progress,
                onPlayAgain = { categoryId ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = categoryId)
                    navController.navigate(WordlyRoutes.game(categoryId)) {
                        popUpTo(WordlyRoutes.CATEGORY_SELECT) { inclusive = false }
                    }
                },
                onNextLevel = { nextCategoryId ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = nextCategoryId)
                    navController.navigate(WordlyRoutes.game(nextCategoryId)) {
                        popUpTo(WordlyRoutes.CATEGORY_SELECT) { inclusive = false }
                    }
                },
                onHome = {
                    viewModel.playTapSound()
                    navController.popBackStack(WordlyRoutes.HOME, inclusive = false)
                },
                onRewardedCoinsEarned = { viewModel.grantRewardedCoins() },
            )
        }
    }
}
