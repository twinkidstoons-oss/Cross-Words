package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.data.BonusDictionary
import com.example.data.CategoriesRepository
import com.example.engine.GridGenerator
import com.example.model.PlayerProgress
import com.example.ui.theme.WordHighlightColors
import com.example.ui.theme.WordlyPuzzleTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
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
    fun `home screen displays title coins and navigates through mode select to game`() {
        composeTestRule.setContent {
            WordlyPuzzleTheme {
                WordlyPuzzleApp()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_title").assertIsDisplayed()
        composeTestRule.onNodeWithText("300").assertIsDisplayed()
        composeTestRule.onNodeWithTag("play_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("categories_grid").assertIsDisplayed()
        composeTestRule.onNodeWithTag("category_card_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("classic_mode_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("word_grid_board").assertIsDisplayed()
    }
}
