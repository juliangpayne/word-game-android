package com.wordgame.app.game

object WordEvaluator {

    const val MAX_ATTEMPTS = 6

    fun evaluateGuess(target: String, guess: String): List<LetterResult> {
        val result = MutableList(guess.length) { LetterResult.ABSENT }
        val targetChars = target.toCharArray()
        val guessChars = guess.toCharArray()
        val used = BooleanArray(target.length)

        for (i in guess.indices) {
            if (guessChars[i] == targetChars[i]) {
                result[i] = LetterResult.CORRECT
                used[i] = true
            }
        }

        for (i in guess.indices) {
            if (result[i] == LetterResult.CORRECT) continue
            val ch = guessChars[i]
            val idx = targetChars.indices.firstOrNull { j ->
                !used[j] && targetChars[j] == ch
            }
            if (idx != null) {
                result[i] = LetterResult.PRESENT
                used[idx] = true
            }
        }
        return result
    }
}
