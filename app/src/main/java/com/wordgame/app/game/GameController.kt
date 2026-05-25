package com.wordgame.app.game

import com.wordgame.app.data.GameLanguage
import com.wordgame.app.data.WordRepository

class GameController(
    private val wordRepository: WordRepository,
    private val language: GameLanguage,
    private val wordLength: Int,
    private val showFirstLetterHint: Boolean,
    private val uniqueLettersOnly: Boolean
) {
    private lateinit var state: GameState

    fun getState(): GameState = state

    fun newGame(): GameState {
        val target = wordRepository.pickRandomWord(language, wordLength, uniqueLettersOnly)
        state = GameState(
            language = language,
            wordLength = wordLength,
            targetWord = target,
            showFirstLetterHint = showFirstLetterHint,
            currentGuess = if (showFirstLetterHint) target.first().toString() else ""
        )
        return state
    }

    fun typeLetter(letter: Char): GameState {
        if (!state.canType || !letter.isLetter()) return state
        state = state.copy(currentGuess = state.currentGuess + letter.lowercaseChar())
        return state
    }

    fun deleteLetter(): GameState {
        if (!state.canDeleteLetter()) return state
        state = state.copy(currentGuess = state.currentGuess.dropLast(1))
        return state
    }

    fun setShowFirstLetterHint(enabled: Boolean): GameState {
        if (state.isComplete || state.showFirstLetterHint == enabled) return state
        val first = state.targetWord.first().toString()
        val newGuess = if (enabled) {
            first + state.currentGuess.removePrefix(first)
        } else {
            state.currentGuess.removePrefix(first)
        }
        state = state.copy(showFirstLetterHint = enabled, currentGuess = newGuess)
        return state
    }

    sealed class SubmitResult {
        data object Success : SubmitResult()
        data object NotInList : SubmitResult()
        data object AlreadyComplete : SubmitResult()
    }

    fun submitGuess(): Pair<GameState, SubmitResult> {
        if (state.isComplete) return state to SubmitResult.AlreadyComplete
        if (!state.canSubmit) return state to SubmitResult.AlreadyComplete

        val guess = WordRepository.normalize(state.currentGuess)
        if (!wordRepository.isValidWord(language, guess, wordLength, uniqueLettersOnly)) {
            return state to SubmitResult.NotInList
        }

        val rowResult = WordEvaluator.evaluateGuess(state.targetWord, guess)
        val newGuesses = state.guesses + guess
        val newResults = state.results + listOf(rowResult)
        val won = guess == state.targetWord
        val nextRow = state.currentRow + 1
        val lost = !won && nextRow >= WordEvaluator.MAX_ATTEMPTS

        state = state.copy(
            currentRow = nextRow,
            currentGuess = state.initialGuess(),
            guesses = newGuesses,
            results = newResults,
            isWon = won,
            isLost = lost,
            isComplete = won || lost
        )
        return state to SubmitResult.Success
    }
}
