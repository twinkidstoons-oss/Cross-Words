package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ads.AdManager
import com.example.model.GameMode
import com.example.ui.screens.CategorySelectScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ModeSelectScreen
import com.example.ui.screens.ResultsScreen
import com.example.ui.theme.WordlyPuzzleTheme
import com.example.viewmodel.WordlyViewModel

object WordlyRoutes {
    const val HOME = "home"
    const val CATEGORY_SELECT = "category_select"
    const val MODE_SELECT = "mode_select/{categoryId}"
    const val GAME = "game/{categoryId}/{mode}"
    const val RESULTS = "results"

    fun modeSelect(categoryId: Int) = "mode_select/$categoryId"
    fun game(categoryId: Int, mode: GameMode) = "game/$categoryId/${mode.routeValue}"
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
    ) {
        composable(WordlyRoutes.HOME) {
            HomeScreen(
                progress = progress,
                onPlayClick = {
                    viewModel.playTapSound()
                    navController.navigate(WordlyRoutes.CATEGORY_SELECT)
                },
                onToggleMute = { viewModel.toggleMute() },
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
                    navController.navigate(WordlyRoutes.modeSelect(category.id))
                },
            )
        }

        composable(
            route = WordlyRoutes.MODE_SELECT,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 1
            ModeSelectScreen(
                categoryId = categoryId,
                progress = progress,
                onBack = {
                    viewModel.playTapSound()
                    navController.popBackStack()
                },
                onModeSelected = { mode ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = categoryId, mode = mode)
                    navController.navigate(WordlyRoutes.game(categoryId, mode))
                },
            )
        }

        composable(
            route = WordlyRoutes.GAME,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("mode") { type = NavType.StringType },
            ),
        ) {
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
                onRewardedHintEarned = { viewModel.grantRewardedHint() },
                onShowMessage = { msg -> viewModel.showTransientMessage(msg) },
                onRoundFinishedNavigate = {
                    navController.navigate(WordlyRoutes.RESULTS) {
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
                onPlayAgain = { categoryId, mode ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = categoryId, mode = mode)
                    navController.navigate(WordlyRoutes.game(categoryId, mode)) {
                        popUpTo(WordlyRoutes.RESULTS) { inclusive = true }
                    }
                },
                onNextLevel = { nextCategoryId, mode ->
                    viewModel.playTapSound()
                    viewModel.startNewGame(categoryId = nextCategoryId, mode = mode)
                    navController.navigate(WordlyRoutes.game(nextCategoryId, mode)) {
                        popUpTo(WordlyRoutes.RESULTS) { inclusive = true }
                    }
                },
                onHome = {
                    viewModel.playTapSound()
                    navController.popBackStack(WordlyRoutes.HOME, inclusive = false)
                },
            )
        }
    }
}
