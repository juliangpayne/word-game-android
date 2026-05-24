package com.wordgame.app.data

enum class GameLanguage(val code: String) {
    ENGLISH("en"),
    FRENCH("fr");

    companion object {
        fun fromCode(code: String): GameLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
