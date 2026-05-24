package com.wordgame.app.data

import android.content.Context

class GamePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var language: GameLanguage
        get() = GameLanguage.fromCode(prefs.getString(KEY_LANGUAGE, GameLanguage.ENGLISH.code)!!)
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value.code).apply()

    var wordLength: Int
        get() = prefs.getInt(KEY_WORD_LENGTH, 5).coerceIn(4, 7)
        set(value) = prefs.edit().putInt(KEY_WORD_LENGTH, value.coerceIn(4, 7)).apply()

    var showFirstLetterHint: Boolean
        get() = prefs.getBoolean(KEY_SHOW_FIRST_LETTER, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_FIRST_LETTER, value).apply()

    var showSolution: Boolean
        get() = prefs.getBoolean(KEY_SHOW_SOLUTION, false)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_SOLUTION, value).apply()

    var uniqueLettersOnly: Boolean
        get() = prefs.getBoolean(KEY_UNIQUE_LETTERS_ONLY, false)
        set(value) = prefs.edit().putBoolean(KEY_UNIQUE_LETTERS_ONLY, value).apply()

    companion object {
        private const val PREFS_NAME = "word_game_prefs"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_WORD_LENGTH = "word_length"
        private const val KEY_SHOW_FIRST_LETTER = "show_first_letter"
        private const val KEY_SHOW_SOLUTION = "show_solution"
        private const val KEY_UNIQUE_LETTERS_ONLY = "unique_letters_only"
    }
}
