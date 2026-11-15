package com.example.engine

import com.example.data.CategoriesRepository
import com.example.model.Category
import com.example.model.GridCoord
import com.example.model.PlacedWord
import com.example.model.PuzzleBoard
import kotlin.random.Random

object GridGenerator {

    const val GRID_SIZE = 8
    const val MIN_TARGET_WORDS = 8
    const val MAX_TARGET_WORDS = 10

    private val directions = listOf(
        Pair(0, 1),   // East
        Pair(0, -1),  // West
        Pair(1, 0),   // South
        Pair(-1, 0),  // North
        Pair(1, 1),   // South-East
        Pair(1, -1),  // South-West
        Pair(-1, 1),  // North-East
        Pair(-1, -1), // North-West
    )

    fun generate(
        category: Category,
        random: Random = Random.Default,
        overrideWords: List<String>? = null,
        exactDesiredCount: Int? = null,
    ): PuzzleBoard {
        val sourceList = overrideWords ?: category.words
        val validWords = CategoriesRepository.validateWords(sourceList)
        val desiredCount = when {
            exactDesiredCount != null -> exactDesiredCount.coerceAtMost(validWords.size)
            validWords.size <= MIN_TARGET_WORDS -> validWords.size
            else -> {
                val upper = minOf(MAX_TARGET_WORDS, validWords.size)
                random.nextInt(MIN_TARGET_WORDS, upper + 1)
            }
        }

        var bestGrid: Array<CharArray>? = null
        var bestPlaced: List<PlacedWord> = emptyList()

        repeat(16) { attempt ->
            val ordered = if (overrideWords != null && attempt == 0) {
                validWords
            } else {
                validWords.shuffled(random)
            }
            val primaryBatch = ordered.take(desiredCount).sortedByDescending { it.length }
            val fallbackBatch = ordered.drop(desiredCount).sortedByDescending { it.length }

            val grid = Array(GRID_SIZE) { CharArray(GRID_SIZE) { '\u0000' } }
            val placed = mutableListOf<PlacedWord>()

            for (word in primaryBatch) {
                tryPlaceWord(word, grid, random)?.let { placed.add(it) }
            }

            if (placed.size < desiredCount) {
                for (fallbackWord in fallbackBatch) {
                    if (placed.size >= desiredCount) break
                    if (placed.none { it.word == fallbackWord }) {
                        tryPlaceWord(fallbackWord, grid, random)?.let { placed.add(it) }
                    }
                }
            }

            if (placed.size > bestPlaced.size) {
                bestPlaced = placed
                bestGrid = grid
            }
            if (placed.size >= desiredCount) {
                return finalizeBoard(grid, placed, random)
            }
        }

        val finalGrid = bestGrid ?: Array(GRID_SIZE) { CharArray(GRID_SIZE) { '\u0000' } }
        return finalizeBoard(finalGrid, bestPlaced, random)
    }

    private fun tryPlaceWord(
        word: String,
        grid: Array<CharArray>,
        random: Random,
    ): PlacedWord? {
        if (word.length !in CategoriesRepository.MIN_WORD_LENGTH..GRID_SIZE) return null

        val allDirections = directions.shuffled(random)
        val startPositions = buildList {
            for (r in 0 until GRID_SIZE) {
                for (c in 0 until GRID_SIZE) {
                    add(Pair(r, c))
                }
            }
        }.shuffled(random)

        for ((dr, dc) in allDirections) {
            for ((sr, sc) in startPositions) {
                val endR = sr + dr * (word.length - 1)
                val endC = sc + dc * (word.length - 1)
                if (endR !in 0 until GRID_SIZE || endC !in 0 until GRID_SIZE) continue

                var canPlace = true
                var overlapCount = 0
                for (i in word.indices) {
                    val r = sr + dr * i
                    val c = sc + dc * i
                    val existing = grid[r][c]
                    if (existing != '\u0000') {
                        if (existing == word[i]) {
                            overlapCount++
                        } else {
                            canPlace = false
                            break
                        }
                    }
                }
                if (canPlace && overlapCount > 0) {
                    return commitWord(word, sr, sc, dr, dc, grid)
                }
            }
        }

        for ((dr, dc) in allDirections) {
            for ((sr, sc) in startPositions) {
                val endR = sr + dr * (word.length - 1)
                val endC = sc + dc * (word.length - 1)
                if (endR !in 0 until GRID_SIZE || endC !in 0 until GRID_SIZE) continue

                var canPlace = true
                for (i in word.indices) {
                    val r = sr + dr * i
                    val c = sc + dc * i
                    val existing = grid[r][c]
                    if (existing != '\u0000' && existing != word[i]) {
                        canPlace = false
                        break
                    }
                }
                if (canPlace) {
                    return commitWord(word, sr, sc, dr, dc, grid)
                }
            }
        }
        return null
    }

    private fun commitWord(
        word: String,
        sr: Int,
        sc: Int,
        dr: Int,
        dc: Int,
        grid: Array<CharArray>,
    ): PlacedWord {
        val coords = ArrayList<GridCoord>(word.length)
        for (i in word.indices) {
            val r = sr + dr * i
            val c = sc + dc * i
            grid[r][c] = word[i]
            coords.add(GridCoord(r, c))
        }
        return PlacedWord(word = word, cells = coords)
    }

    private fun finalizeBoard(
        grid: Array<CharArray>,
        placedWords: List<PlacedWord>,
        random: Random,
    ): PuzzleBoard {
        val rows = List(GRID_SIZE) { r ->
            List(GRID_SIZE) { c ->
                val ch = grid[r][c]
                if (ch in 'A'..'Z') ch else ('A' + random.nextInt(26))
            }
        }
        return PuzzleBoard(
            size = GRID_SIZE,
            grid = rows,
            placedWords = placedWords.sortedBy { it.word },
        )
    }
}
