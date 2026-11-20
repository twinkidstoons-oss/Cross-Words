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
    val isDailyChallenge: Boolean = false,
    val dailyChallengeStage: Int = 1,
    val dailyChallengeTotalStages: Int = GameUiState.DAILY_CHALLENGE_TOTAL_STAGES,
    val isDailyChallengeCompleted: Boolean = false,
)

data class PlayerProgress(
    val coins: Int = 300,
    val completedCategoryIds: Set<Int> = emptySet(),
    val isMuted: Boolean = false,
    val isLoaded: Boolean = false,
    val dailyChallengeCompletionTimestamps: List<Long> = emptyList(),
) {
    companion object {
        const val DAILY_CHALLENGE_MAX_PER_24H = 2
        const val DAILY_CHALLENGE_WINDOW_MS = 24L * 60L * 60L * 1000L
        const val DAILY_CHALLENGE_COOLDOWN_MS = 12L * 60L * 60L * 1000L
    }

    fun isCategoryUnlocked(categoryId: Int): Boolean {
        if (categoryId <= 1) return true
        return completedCategoryIds.contains(categoryId - 1)
    }

    fun isCategoryCompleted(categoryId: Int): Boolean =
        completedCategoryIds.contains(categoryId)

    fun recentDailyTimestamps(nowMs: Long = System.currentTimeMillis()): List<Long> =
        dailyChallengeCompletionTimestamps
            .filter { ts -> ts <= nowMs && (nowMs - ts) < DAILY_CHALLENGE_WINDOW_MS }
            .sorted()

    fun dailyChallengesUsedIn24h(nowMs: Long = System.currentTimeMillis()): Int =
        recentDailyTimestamps(nowMs).size.coerceAtMost(DAILY_CHALLENGE_MAX_PER_24H)

    fun dailyChallengesRemainingIn24h(nowMs: Long = System.currentTimeMillis()): Int =
        (DAILY_CHALLENGE_MAX_PER_24H - dailyChallengesUsedIn24h(nowMs)).coerceAtLeast(0)

    fun dailyChallengeRemainingLockMs(nowMs: Long = System.currentTimeMillis()): Long {
        val lastCompletion = dailyChallengeCompletionTimestamps
            .filter { it <= nowMs }
            .maxOrNull()
        val cooldownRemaining = lastCompletion
            ?.let { (it + DAILY_CHALLENGE_COOLDOWN_MS) - nowMs }
            ?.coerceAtLeast(0L)
            ?: 0L

        val recent = recentDailyTimestamps(nowMs)
        val windowLimitRemaining = if (recent.size >= DAILY_CHALLENGE_MAX_PER_24H) {
            ((recent.first() + DAILY_CHALLENGE_WINDOW_MS) - nowMs).coerceAtLeast(0L)
        } else {
            0L
        }

        return maxOf(cooldownRemaining, windowLimitRemaining)
    }

    fun isDailyChallengeAvailable(nowMs: Long = System.currentTimeMillis()): Boolean =
        dailyChallengeRemainingLockMs(nowMs) == 0L && dailyChallengesRemainingIn24h(nowMs) > 0
}

data class GameUiState(
    val category: Category,
    val board: PuzzleBoard,
    val foundWords: List<FoundWordEntry> = emptyList(),
    val bonusWordsFound: Set<String> = emptySet(),
    val score: Int = 0,
    val wordPoints: Int = 0,
    val bonusWordPoints: Int = 0,
    val bonusCoinsEarned: Int = 0,
    val freeHintsRemaining: Int = MAX_FREE_HINTS,
    val hintedCells: Set<GridCoord> = emptySet(),
    val hintPulseStep: Int = 0,
    val wrongSelectionCells: Set<GridCoord> = emptySet(),
    val elapsedSeconds: Int = 0,
    val bonusToastText: String? = null,
    val infoMessage: String? = null,
    val isRoundFinished: Boolean = false,
    val isDailyChallenge: Boolean = false,
    val dailyChallengeStage: Int = 1,
    val dailyChallengeCategories: List<Int> = emptyList(),
) {
    companion object {
        const val MAX_FREE_HINTS = 3
        const val POINTS_PER_WORD = 10
        const val POINTS_PER_BONUS_WORD = 5
        const val COINS_PER_WIN = 50
        const val COINS_PER_BONUS_WORD = 20
        const val DAILY_CHALLENGE_TOTAL_STAGES = 3
        const val DAILY_CHALLENGE_REWARD_COINS = 500
    }

    val foundWordNames: Set<String>
        get() = foundWords.map { it.word }.toSet()
}
