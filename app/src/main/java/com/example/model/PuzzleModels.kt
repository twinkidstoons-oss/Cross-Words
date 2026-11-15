package com.example.model

data class Category(
    val id: Int, // 1..25
    val name: String,
    val emoji: String,
    val words: List<String>,
)

data class GridCoord(
    val row: Int,
    val col: Int,
)

data class PlacedWord(
    val word: String,
    val cells: List<GridCoord>,
)

data class PuzzleBoard(
    val size: Int = 8,
    val grid: List<List<Char>>,
    val placedWords: List<PlacedWord>,
)

data class FoundWordEntry(
    val word: String,
    val cells: List<GridCoord>,
    val colorIndex: Int,
)

data class RoundResult(
    val categoryId: Int,
    val categoryName: String,
    val categoryEmoji: String,
    val completed: Boolean,
    val totalScore: Int,
    val wordPoints: Int,
    val bonusWordPoints: Int,
    val wordsFoundCount: Int,
    val totalWordsCount: Int,
    val bonusWordsCount: Int,
    val coinsEarned: Int,
    val elapsedSeconds: Int,
    val hasNextLevel: Boolean,
)

data class PlayerProgress(
    val coins: Int = 300,
    val completedCategoryIds: Set<Int> = emptySet(),
    val isMuted: Boolean = false,
    val isLoaded: Boolean = false,
) {
    fun isCategoryUnlocked(categoryId: Int): Boolean {
        if (categoryId <= 1) return true
        return completedCategoryIds.contains(categoryId - 1)
    }

    fun isCategoryCompleted(categoryId: Int): Boolean =
        completedCategoryIds.contains(categoryId)
}

data class GameUiState(
    val category: Category,
    val board: PuzzleBoard,
    val foundWords: List<FoundWordEntry> = emptyList(),
    val bonusWordsFound: Set<String> = emptySet(),
    val score: Int = 0,
    val wordPoints: Int = 0,
    val bonusWordPoints: Int = 0,
    val freeHintsRemaining: Int = MAX_FREE_HINTS,
    val hintedCells: Set<GridCoord> = emptySet(),
    val hintPulseStep: Int = 0,
    val wrongSelectionCells: Set<GridCoord> = emptySet(),
    val elapsedSeconds: Int = 0,
    val bonusToastText: String? = null,
    val infoMessage: String? = null,
    val isRoundFinished: Boolean = false,
) {
    companion object {
        const val MAX_FREE_HINTS = 3
        const val POINTS_PER_WORD = 10
        const val POINTS_PER_BONUS_WORD = 5
        const val COINS_PER_WIN = 50
    }

    val foundWordNames: Set<String>
        get() = foundWords.map { it.word }.toSet()
}
