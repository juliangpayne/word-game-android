package com.wordgame.app.game

import com.wordgame.app.data.GameLanguage

data class GameState(
    val language: GameLanguage,
    val wordLength: Int,
    val targetWord: String,
    val showFirstLetterHint: Boolean = false,
    val currentRow: Int = 0,
    val currentGuess: String = "",
    val guesses: List<String> = emptyList(),
    val results: List<List<LetterResult>> = emptyList(),
    val isWon: Boolean = false,
    val isLost: Boolean = false,
    val isComplete: Boolean = false
) {
    private val minGuessLength: Int
        get() = if (showFirstLetterHint) 1 else 0

    val canType: Boolean
        get() = !isComplete && currentGuess.length < wordLength

    val canSubmit: Boolean
        get() = !isComplete && currentGuess.length == wordLength

    fun initialGuess(): String =
        if (showFirstLetterHint) targetWord.first().toString() else ""

    fun canDeleteLetter(): Boolean = currentGuess.length > minGuessLength
}
