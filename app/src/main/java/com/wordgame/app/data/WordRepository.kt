package com.wordgame.app.data

import android.content.Context
import java.text.Normalizer

class WordRepository(private val context: Context) {

    private val cache = mutableMapOf<String, Set<String>>()

    fun loadWords(language: GameLanguage, length: Int, uniqueLettersOnly: Boolean = false): Set<String> {
        val key = "${language.code}_${length}_u$uniqueLettersOnly"
        return cache.getOrPut(key) {
            val all = loadWordsFromAssets(language, length)
            if (uniqueLettersOnly) all.filter { hasUniqueLetters(it) }.toSet() else all
        }
    }

    private fun loadWordsFromAssets(language: GameLanguage, length: Int): Set<String> {
        val key = "${language.code}_$length"
        return cache.getOrPut(key) {
            val assetName = "words/${language.code}_$length.txt"
            context.assets.open(assetName).bufferedReader().useLines { lines ->
                lines.map { it.trim().lowercase() }
                    .filter { it.length == length && it.all { c -> c in 'a'..'z' } }
                    .toSet()
            }
        }
    }

    fun pickRandomWord(language: GameLanguage, length: Int, uniqueLettersOnly: Boolean): String {
        val words = loadWords(language, length, uniqueLettersOnly)
        check(words.isNotEmpty()) {
            "No words available for ${language.code} length $length (uniqueLettersOnly=$uniqueLettersOnly)"
        }
        return words.random()
    }

    fun isValidWord(
        language: GameLanguage,
        word: String,
        length: Int,
        uniqueLettersOnly: Boolean
    ): Boolean {
        val normalized = normalize(word)
        return normalized.length == length &&
            loadWords(language, length, uniqueLettersOnly).contains(normalized)
    }

    companion object {
        fun hasUniqueLetters(word: String): Boolean = word.length == word.toSet().size

        fun normalize(word: String): String {
            val lower = word.trim().lowercase()
            val normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
            return buildString(normalized.length) {
                for (ch in normalized) {
                    if (Character.getType(ch).toByte() != Character.NON_SPACING_MARK) {
                        append(ch)
                    }
                }
            }
        }
    }
}
