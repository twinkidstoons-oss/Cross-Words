package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsManager
import com.example.data.BonusDictionary
import com.example.data.CategoriesRepository
import com.example.data.PlayerProgressStore
import com.example.engine.GridGenerator
import com.example.model.GameMode
import com.example.model.GameUiState
import com.example.model.GridCoord
import com.example.model.PlayerProgress
import com.example.model.RoundResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WordlyViewModel(application: Application) : AndroidViewModel(application) {

    private val progressStore = PlayerProgressStore(application.applicationContext)
    val soundManager = SoundEffectsManager(application.applicationContext)

    val playerProgress: StateFlow<PlayerProgress> = progressStore.progressFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = PlayerProgress(),
        )

    private val _gameState = MutableStateFlow<GameUiState?>(null)
    val gameState: StateFlow<GameUiState?> = _gameState.asStateFlow()

    private val _roundResult = MutableStateFlow<RoundResult?>(null)
    val roundResult: StateFlow<RoundResult?> = _roundResult.asStateFlow()

    private var timerJob: Job? = null
    private var hintAnimJob: Job? = null
    private var wrongFeedbackJob: Job? = null
    private var bonusToastJob: Job? = null
    private var infoMsgJob: Job? = null

    private var rewardClaimedForCurrentRound = false

    init {
        viewModelScope.launch {
            playerProgress.collect { progress ->
                soundManager.isMuted = progress.isMuted
            }
        }
    }

    fun toggleMute() {
        val nextMuted = !playerProgress.value.isMuted
        soundManager.isMuted = nextMuted
        viewModelScope.launch {
            progressStore.setMuted(nextMuted)
            if (!nextMuted) {
                soundManager.playTap()
            }
        }
    }

    fun playTapSound() {
        soundManager.playTap()
    }

    fun playSwipeSound() {
        soundManager.playSwipe()
    }

    fun startNewGame(categoryId: Int, mode: GameMode) {
        stopTimersAndJobs()
        rewardClaimedForCurrentRound = false
        _roundResult.value = null

        val category = CategoriesRepository.getById(categoryId)
        val board = GridGenerator.generate(category)

        _gameState.value = GameUiState(
            category = category,
            mode = mode,
            board = board,
            remainingSeconds = GameUiState.TIME_MODE_DURATION_SECONDS,
        )
        startTimer()
    }

    fun pauseOrStopGame() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _gameState.value ?: break
                if (current.isRoundFinished) break

                if (current.mode == GameMode.CLASSIC) {
                    _gameState.update { state ->
                        state?.copy(elapsedSeconds = state.elapsedSeconds + 1)
                    }
                } else {
                    val nextRemaining = (current.remainingSeconds - 1).coerceAtLeast(0)
                    val nextElapsed = current.elapsedSeconds + 1
                    _gameState.update { state ->
                        state?.copy(
                            elapsedSeconds = nextElapsed,
                            remainingSeconds = nextRemaining,
                        )
                    }
                    if (nextRemaining in 1..29) {
                        soundManager.playTick()
                    }
                    if (nextRemaining <= 0) {
                        soundManager.playTimeOver()
                        finishRound(completed = false)
                        break
                    }
                }
            }
        }
    }

    fun submitSelection(selectedCells: List<GridCoord>) {
        val state = _gameState.value ?: return
        if (state.isRoundFinished || selectedCells.isEmpty()) return

        if (selectedCells.size < 2) return

        val selectedWord = buildString(selectedCells.size) {
            for (coord in selectedCells) {
                append(state.board.grid[coord.row][coord.col])
            }
        }
        val reversedWord = selectedWord.reversed()

        // 1. Check if selection matches an unfound target word (forward or reverse drag)
        val matchedTarget = state.board.placedWords.firstOrNull { placed ->
            placed.word !in state.foundWordNames &&
                (placed.cells == selectedCells || placed.cells.reversed() == selectedCells) &&
                (placed.word == selectedWord || placed.word == reversedWord)
        }

        if (matchedTarget != null) {
            val nextColorIdx = state.foundWords.size
            val newFoundEntry = com.example.model.FoundWordEntry(
                word = matchedTarget.word,
                cells = matchedTarget.cells,
                colorIndex = nextColorIdx,
            )
            val updatedFound = state.foundWords + newFoundEntry
            val updatedWordPoints = state.wordPoints + GameUiState.POINTS_PER_WORD
            val updatedScore = updatedWordPoints + state.bonusWordPoints
            val allFound = updatedFound.size >= state.board.placedWords.size

            _gameState.update { s ->
                s?.copy(
                    foundWords = updatedFound,
                    wordPoints = updatedWordPoints,
                    score = updatedScore,
                    hintedCells = if (matchedTarget.cells.any { it in s.hintedCells }) emptySet() else s.hintedCells,
                )
            }

            if (allFound) {
                soundManager.playWin()
                finishRound(completed = true)
            } else {
                soundManager.playCorrect()
            }
            return
        }

        // 2. Check if selection matches an already-found target word (ignore gently)
        val alreadyFound = state.board.placedWords.any { placed ->
            placed.word in state.foundWordNames &&
                (placed.cells == selectedCells || placed.cells.reversed() == selectedCells)
        }
        if (alreadyFound) {
            return
        }

        // 3. Check Bonus Word (offline dictionary >= 3 letters, not a puzzle target word, scored once)
        val targetWordSet = state.board.placedWords.map { it.word }.toSet()
        val candidateBonus = when {
            BonusDictionary.isBonusWord(selectedWord, targetWordSet) &&
                selectedWord !in state.bonusWordsFound -> selectedWord
            BonusDictionary.isBonusWord(reversedWord, targetWordSet) &&
                reversedWord !in state.bonusWordsFound -> reversedWord
            else -> null
        }

        if (candidateBonus != null) {
            soundManager.playBonus()
            val updatedBonusSet = state.bonusWordsFound + candidateBonus
            val updatedBonusPoints = state.bonusWordPoints + GameUiState.POINTS_PER_BONUS_WORD
            val updatedScore = state.wordPoints + updatedBonusPoints

            _gameState.update { s ->
                s?.copy(
                    bonusWordsFound = updatedBonusSet,
                    bonusWordPoints = updatedBonusPoints,
                    score = updatedScore,
                    bonusToastText = "+5 Bonus: $candidateBonus",
                )
            }
            scheduleClearBonusToast()
            return
        }

        // 4. Wrong selection feedback
        soundManager.playWrong()
        wrongFeedbackJob?.cancel()
        _gameState.update { it?.copy(wrongSelectionCells = selectedCells.toSet()) }
        wrongFeedbackJob = viewModelScope.launch {
            delay(280L)
            _gameState.update { it?.copy(wrongSelectionCells = emptySet()) }
        }
    }

    fun useHint() {
        val state = _gameState.value ?: return
        if (state.isRoundFinished || state.freeHintsRemaining <= 0) return

        val unfoundWords = state.board.placedWords.filter { it.word !in state.foundWordNames }
        if (unfoundWords.isEmpty()) return

        val chosen = unfoundWords.random()
        soundManager.playTap()

        _gameState.update { s ->
            s?.copy(
                freeHintsRemaining = (s.freeHintsRemaining - 1).coerceAtLeast(0),
                hintedCells = chosen.cells.toSet(),
                hintPulseStep = 1,
            )
        }
        triggerHintPulseAnimation()
    }

    fun grantRewardedHint() {
        soundManager.playBonus()
        _gameState.update { s ->
            s?.copy(
                freeHintsRemaining = s.freeHintsRemaining + 1,
                infoMessage = "+1 Hint added!",
            )
        }
        scheduleClearInfoMessage()
    }

    fun showTransientMessage(message: String) {
        _gameState.update { it?.copy(infoMessage = message) }
        scheduleClearInfoMessage()
    }

    private fun triggerHintPulseAnimation() {
        hintAnimJob?.cancel()
        hintAnimJob = viewModelScope.launch {
            // 3 distinct pulses over ~1.5 seconds (500ms per pulse cycle)
            for (step in 1..6) {
                _gameState.update { it?.copy(hintPulseStep = step) }
                delay(250L)
            }
            _gameState.update { it?.copy(hintedCells = emptySet(), hintPulseStep = 0) }
        }
    }

    private fun scheduleClearBonusToast() {
        bonusToastJob?.cancel()
        bonusToastJob = viewModelScope.launch {
            delay(1600L)
            _gameState.update { it?.copy(bonusToastText = null) }
        }
    }

    private fun scheduleClearInfoMessage() {
        infoMsgJob?.cancel()
        infoMsgJob = viewModelScope.launch {
            delay(1800L)
            _gameState.update { it?.copy(infoMessage = null) }
        }
    }

    private fun finishRound(completed: Boolean) {
        timerJob?.cancel()
        val state = _gameState.value ?: return
        if (state.isRoundFinished) return

        val timeBonus = if (completed && state.mode == GameMode.TIME) {
            state.remainingSeconds * 2
        } else {
            0
        }
        val finalScore = state.wordPoints + state.bonusWordPoints + timeBonus

        val coinsAwarded = if (completed && !rewardClaimedForCurrentRound) {
            rewardClaimedForCurrentRound = true
            GameUiState.COINS_PER_WIN
        } else {
            0
        }

        if (coinsAwarded > 0) {
            viewModelScope.launch {
                progressStore.recordLevelCompleted(
                    categoryId = state.category.id,
                    coinsReward = coinsAwarded,
                )
            }
        }

        val hasNext = state.category.id < CategoriesRepository.categories.size
        _roundResult.value = RoundResult(
            categoryId = state.category.id,
            categoryName = state.category.name,
            categoryEmoji = state.category.emoji,
            mode = state.mode,
            completed = completed,
            totalScore = finalScore,
            wordPoints = state.wordPoints,
            bonusWordPoints = state.bonusWordPoints,
            timeBonusPoints = timeBonus,
            wordsFoundCount = state.foundWords.size,
            totalWordsCount = state.board.placedWords.size,
            bonusWordsCount = state.bonusWordsFound.size,
            coinsEarned = coinsAwarded,
            elapsedSeconds = state.elapsedSeconds,
            remainingSeconds = state.remainingSeconds,
            hasNextLevel = hasNext,
        )

        _gameState.update { s ->
            s?.copy(
                score = finalScore,
                isRoundFinished = true,
            )
        }
    }

    private fun stopTimersAndJobs() {
        timerJob?.cancel()
        hintAnimJob?.cancel()
        wrongFeedbackJob?.cancel()
        bonusToastJob?.cancel()
        infoMsgJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimersAndJobs()
        soundManager.release()
    }
}
