package com.example

import android.app.Application
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.BonusDictionary
import com.example.data.CategoriesRepository
import com.example.engine.GridGenerator
import com.example.model.GridCoord
import com.example.model.PlayerProgress
import com.example.model.PuzzleBoard
import com.example.ui.theme.WordHighlightColors
import com.example.ui.theme.WordlyPuzzleTheme
import com.example.viewmodel.WordlyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WordlyPuzzleTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `all 25 categories exist in order with valid 3-8 letter words`() {
        val expectedOrder = listOf(
            "Animals", "Food", "Sports", "Countries", "Colors",
            "Fruits", "Space", "Ocean Life", "Music", "Movies",
            "Nature", "Technology", "Jobs", "Clothing", "Vegetables",
            "Transport", "Weather", "School", "Home", "Insects",
            "Body Parts", "Emotions", "Sea & Beach", "Gadgets", "Flowers",
        )
        assertEquals(25, CategoriesRepository.categories.size)
        assertEquals(expectedOrder, CategoriesRepository.categories.map { it.name })

        for (category in CategoriesRepository.categories) {
            assertTrue("Category ${category.name} has words", category.words.isNotEmpty())
            assertEquals(category.words.distinct().size, category.words.size)
            for (word in category.words) {
                assertTrue(
                    "Word $word length must be 3..8",
                    word.length in 3..8 && word.all { it in 'A'..'Z' },
                )
            }
        }
    }

    @Test
    fun `bonus dictionary contains at least 600 words and enforces rules`() {
        assertTrue(
            "Expected >= 600 bonus words, found ${BonusDictionary.words.size}",
            BonusDictionary.words.size >= 600,
        )
        val targets = setOf("CAT", "DOG")
        assertFalse(BonusDictionary.isBonusWord("CAT", targets))
        assertFalse(BonusDictionary.isBonusWord("AT", targets))
        assertTrue(BonusDictionary.isBonusWord("ACT", targets))
    }

    @Test
    fun `grid generator produces valid 8x8 boards for all 25 categories`() {
        val random = Random(42)
        for (category in CategoriesRepository.categories) {
            val board = GridGenerator.generate(category, random)
            assertEquals(8, board.size)
            assertEquals(8, board.grid.size)
            board.grid.forEach { row ->
                assertEquals(8, row.size)
                row.forEach { ch -> assertTrue(ch in 'A'..'Z') }
            }
            assertTrue(
                "Category ${category.name} placed ${board.placedWords.size} words",
                board.placedWords.size in 8..10,
            )
            for (placed in board.placedWords) {
                assertEquals(placed.word.length, placed.cells.size)
                val reconstructed = buildString {
                    for (coord in placed.cells) {
                        assertTrue(coord.row in 0..7)
                        assertTrue(coord.col in 0..7)
                        append(board.grid[coord.row][coord.col])
                    }
                }
                assertEquals(placed.word, reconstructed)
            }
        }
    }

    @Test
    fun `18 highlight colors and progression unlocking work correctly`() {
        assertEquals(18, WordHighlightColors.size)
        val initial = PlayerProgress()
        assertEquals(300, initial.coins)
        assertTrue(initial.isCategoryUnlocked(1))
        assertFalse(initial.isCategoryUnlocked(2))

        val afterFirst = initial.copy(completedCategoryIds = setOf(1))
        assertTrue(afterFirst.isCategoryUnlocked(2))
        assertFalse(afterFirst.isCategoryUnlocked(3))
    }

    @Test
    fun `home screen displays title coins and navigates directly from category to game`() {
        composeTestRule.setContent {
            WordlyPuzzleTheme {
                WordlyPuzzleApp()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("8×8", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("coin_balance_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("coin_plus_icon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_challenge_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("categories_grid").assertIsDisplayed()
        composeTestRule.onNodeWithText("8×8", substring = true).assertDoesNotExist()
        composeTestRule.onNodeWithTag("coin_balance_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_card_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Level 1", substring = true)
        composeTestRule.onNodeWithTag("word_grid_board").assertIsDisplayed()
        composeTestRule.onNodeWithTag("hint_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("buy_hint_coins_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("watch_ad_coins_button").assertDoesNotExist()
    }

    @Test
    fun `clicking coins pill opens watch ad 500 dialog depleted free hints show AD dialog and level 1 advances to level 2`() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        val vm = WordlyViewModel(appContext)
        composeTestRule.setContent {
            WordlyPuzzleTheme {
                WordlyPuzzleApp(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()
        if (vm.playerProgress.value.coins < 300) {
            composeTestRule.runOnIdle { vm.grantRewardedCoins() }
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                shadowOf(Looper.getMainLooper()).idle()
                vm.playerProgress.value.coins >= 300
            }
        }

        // Coin pill shows '+' icon and opens Watch Ad (+500) dialog
        composeTestRule.onNodeWithTag("coin_plus_icon", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("coin_balance_pill").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("watch_ad_500_dialog_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Close").performClick()
        composeTestRule.waitForIdle()

        // Direct play: Home -> Category 1 -> Level 1 GameScreen
        composeTestRule.onNodeWithTag("play_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("category_card_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Level 1", substring = true)
        composeTestRule.onNodeWithTag("buy_hint_coins_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("watch_ad_coins_button").assertDoesNotExist()

        // Use all 3 free hints
        repeat(3) {
            composeTestRule.onNodeWithTag("hint_button").performClick()
            composeTestRule.waitForIdle()
        }

        // Free hints are 0 -> AD badge appears on the single hint button
        composeTestRule.onNodeWithTag("hint_ad_badge", useUnmergedTree = true).assertIsDisplayed()

        // Clicking hint button opens 2-option dialog: Watch Ad or 300 Coins
        val coinsBeforeHint = vm.playerProgress.value.coins
        composeTestRule.onNodeWithTag("hint_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dialog_hint_watch_ad_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dialog_hint_300_coins_button").assertIsDisplayed().performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            vm.playerProgress.value.coins == coinsBeforeHint - 300
        }
        composeTestRule.waitForIdle()

        // Complete Level 1 and advance directly to Level 2
        val placedWords = vm.gameState.value!!.board.placedWords
        composeTestRule.runOnIdle {
            for (word in placedWords) {
                vm.submitSelection(word.cells)
            }
        }

        composeTestRule.mainClock.advanceTimeBy(600L)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            vm.roundResult.value != null
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("results_title").assertTextContains("Level Complete!")
        composeTestRule.onNodeWithTag("next_level_button").assertTextContains("Next: Level 2", substring = true)
        composeTestRule.onNodeWithTag("next_level_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Level 2", substring = true)
        composeTestRule.onNodeWithTag("word_grid_board").assertIsDisplayed()
    }

    @Test
    fun `daily challenge enforces 2 runs per 24h and 12h cooldown`() {
        val t0 = 1_700_000_000_000L
        val twelveHours = PlayerProgress.DAILY_CHALLENGE_COOLDOWN_MS
        val twentyFourHours = PlayerProgress.DAILY_CHALLENGE_WINDOW_MS

        val initial = PlayerProgress()
        assertTrue(initial.isDailyChallengeAvailable(t0))
        assertEquals(2, initial.dailyChallengesRemainingIn24h(t0))
        assertEquals(0L, initial.dailyChallengeRemainingLockMs(t0))

        // After completing 1st Daily Challenge at t0 -> 12h cooldown starts, 1/2 remaining
        val afterFirst = initial.copy(dailyChallengeCompletionTimestamps = listOf(t0))
        assertFalse(afterFirst.isDailyChallengeAvailable(t0 + 1_000L))
        assertEquals(twelveHours - 1_000L, afterFirst.dailyChallengeRemainingLockMs(t0 + 1_000L))
        assertEquals(1, afterFirst.dailyChallengesRemainingIn24h(t0 + 1_000L))

        // At t0 + 12h -> unlocked for 2nd run of the 24h period
        assertTrue(afterFirst.isDailyChallengeAvailable(t0 + twelveHours))
        assertEquals(1, afterFirst.dailyChallengesRemainingIn24h(t0 + twelveHours))

        // After completing 2nd Daily Challenge at t0 + 12h -> 0/2 left, locked for 12h (until t0 + 24h)
        val t1 = t0 + twelveHours
        val afterSecond = afterFirst.copy(dailyChallengeCompletionTimestamps = listOf(t0, t1))
        assertFalse(afterSecond.isDailyChallengeAvailable(t1 + 5_000L))
        assertEquals(0, afterSecond.dailyChallengesRemainingIn24h(t1 + 5_000L))
        assertEquals(twelveHours - 5_000L, afterSecond.dailyChallengeRemainingLockMs(t1 + 5_000L))

        // At t0 + 24h -> first run aged out of 24h window & 12h cooldown passed -> available again
        assertTrue(afterSecond.isDailyChallengeAvailable(t0 + twentyFourHours))
        assertEquals(1, afterSecond.dailyChallengesRemainingIn24h(t0 + twentyFourHours))
    }

    @Test
    fun `bonus words award 20 coins and daily challenge 3 levels award 500 coins and start 12h timer`() {
        val appContext = ApplicationProvider.getApplicationContext<Application>()
        val vm = WordlyViewModel(appContext)
        var fakeNow = 1_800_000_000_000L
        vm.currentTimeMillisProvider = { fakeNow }

        composeTestRule.setContent {
            WordlyPuzzleTheme {
                WordlyPuzzleApp(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()

        // 1. Navigate to normal Level 1 and verify +20 coins for extra bonus word
        composeTestRule.onNodeWithTag("play_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("category_card_1").performClick()
        composeTestRule.waitForIdle()
        val normalBoard = vm.gameState.value!!.board
        val normalBonusCells = findAnyBonusWordCells(normalBoard)
        if (normalBonusCells != null) {
            val coinsBeforeNormalBonus = vm.playerProgress.value.coins
            composeTestRule.runOnIdle { vm.submitSelection(normalBonusCells) }
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                shadowOf(Looper.getMainLooper()).idle()
                vm.playerProgress.value.coins == coinsBeforeNormalBonus + 20
            }
        }

        // 2. Navigate back to Home (Game -> Categories -> Home) and start Daily Challenge
        composeTestRule.onNodeWithTag("back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("daily_challenge_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Stage 1/3 -> also test +20 coins for bonus word in challenge level
        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Daily 1/3", substring = true)
        val stage1Board = vm.gameState.value!!.board
        val stage1BonusCells = findAnyBonusWordCells(stage1Board)
        if (stage1BonusCells != null) {
            val coinsBeforeChallengeBonus = vm.playerProgress.value.coins
            composeTestRule.runOnIdle { vm.submitSelection(stage1BonusCells) }
            composeTestRule.waitUntil(timeoutMillis = 5_000) {
                shadowOf(Looper.getMainLooper()).idle()
                vm.playerProgress.value.coins == coinsBeforeChallengeBonus + 20
            }
        }

        // Solve Stage 1 -> advance to Stage 2/3
        solveCurrentBoardAndWaitResults(vm)
        composeTestRule.onNodeWithTag("results_title").assertTextContains("Stage 1/3 Complete!")
        composeTestRule.onNodeWithTag("next_level_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Solve Stage 2 -> advance to Stage 3/3
        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Daily 2/3", substring = true)
        solveCurrentBoardAndWaitResults(vm)
        composeTestRule.onNodeWithTag("results_title").assertTextContains("Stage 2/3 Complete!")
        composeTestRule.onNodeWithTag("next_level_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Solve Stage 3 -> +500 coins earned and 12h cooldown timer starts
        composeTestRule.onNodeWithTag("game_level_title").assertTextContains("Daily 3/3", substring = true)
        val coinsBeforeDailyFinish = vm.playerProgress.value.coins
        solveCurrentBoardAndWaitResults(vm)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            vm.playerProgress.value.coins == coinsBeforeDailyFinish + 500
        }
        composeTestRule.onNodeWithTag("results_title").assertTextContains("Daily Challenge Complete!")
        composeTestRule.onNodeWithTag("home_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Home screen now shows 12h timer and blocks starting another run immediately
        composeTestRule.onNodeWithTag("daily_challenge_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("daily_challenge_timer", useUnmergedTree = true).assertIsDisplayed()
        assertNull(vm.startDailyChallenge())
        assertTrue(vm.playerProgress.value.dailyChallengeRemainingLockMs(fakeNow) > 0L)
    }

    private fun solveCurrentBoardAndWaitResults(vm: WordlyViewModel) {
        val placedWords = vm.gameState.value!!.board.placedWords
        composeTestRule.runOnIdle {
            for (word in placedWords) {
                vm.submitSelection(word.cells)
            }
        }
        composeTestRule.mainClock.advanceTimeBy(600L)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            shadowOf(Looper.getMainLooper()).idle()
            vm.roundResult.value != null && vm.gameState.value?.isRoundFinished == true
        }
        composeTestRule.waitForIdle()
    }

    private fun findAnyBonusWordCells(board: PuzzleBoard): List<GridCoord>? {
        val targets = board.placedWords.map { it.word }.toSet()
        val coordsByLetter = mutableMapOf<Char, MutableList<GridCoord>>()
        for (r in 0 until board.size) {
            for (c in 0 until board.size) {
                coordsByLetter.getOrPut(board.grid[r][c]) { mutableListOf() }.add(GridCoord(r, c))
            }
        }
        for (word in BonusDictionary.words) {
            if (!BonusDictionary.isBonusWord(word, targets)) continue
            val used = mutableSetOf<GridCoord>()
            val path = mutableListOf<GridCoord>()
            var ok = true
            for (ch in word) {
                val candidate = coordsByLetter[ch]?.firstOrNull { it !in used }
                if (candidate == null) {
                    ok = false
                    break
                }
                used.add(candidate)
                path.add(candidate)
            }
            if (ok) return path
        }
        return null
    }
}
