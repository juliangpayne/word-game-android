package com.wordgame.app.ui

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.wordgame.app.R
import com.wordgame.app.data.GameLanguage
import com.wordgame.app.game.LetterResult

class GameKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface Listener {
        fun onLetter(letter: Char)
        fun onBackspace()
        fun onEnter()
    }

    private var listener: Listener? = null
    private val keyButtons = mutableMapOf<Char, Button>()
    private var language: GameLanguage = GameLanguage.ENGLISH

    init {
        orientation = VERTICAL
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    fun setLanguage(language: GameLanguage) {
        if (this.language == language && childCount > 0) return
        this.language = language
        buildKeyboard()
    }

    fun updateKeyStates(letterStates: Map<Char, LetterResult>) {
        for ((letter, button) in keyButtons) {
            applyKeyState(button, letterStates[letter])
        }
    }

    fun resetKeyStates() {
        for (button in keyButtons.values) {
            applyKeyState(button, null)
        }
    }

    private fun applyKeyState(button: Button, state: LetterResult?) {
        when (state) {
            LetterResult.CORRECT -> {
                button.background = null
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.letter_correct))
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            LetterResult.PRESENT -> {
                button.background = null
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.letter_present))
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            LetterResult.ABSENT -> {
                button.background = null
                button.setBackgroundColor(ContextCompat.getColor(context, R.color.letter_absent))
                button.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> {
                button.background = ContextCompat.getDrawable(context, R.drawable.keyboard_key_bg)
                button.setTextColor(ContextCompat.getColor(context, R.color.keyboard_key_text))
            }
        }
    }

    private fun buildKeyboard() {
        removeAllViews()
        keyButtons.clear()

        val (topRow, middleRow, bottomRow) = when (language) {
            GameLanguage.ENGLISH -> Triple("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
            GameLanguage.FRENCH -> Triple("AZERTYUIOP", "QSDFGHJKLM", "WXCVBN")
        }

        val density = resources.displayMetrics.density
        val keyHeight = (density * 48).toInt()
        val keyMargin = (density * 3).toInt()

        addLetterRow(topRow, keyHeight, keyMargin)
        addLetterRow(middleRow, keyHeight, keyMargin)

        val actionRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        val enterBtn = createActionKey(
            context.getString(R.string.enter),
            (density * 64).toInt(),
            keyHeight,
            keyMargin
        ) { listener?.onEnter() }
        actionRow.addView(enterBtn)

        for (ch in bottomRow) {
            actionRow.addView(createLetterKey(ch, keyHeight, keyMargin))
        }

        val backspaceBtn = createActionKey(
            context.getString(R.string.backspace),
            (density * 52).toInt(),
            keyHeight,
            keyMargin
        ) { listener?.onBackspace() }
        actionRow.addView(backspaceBtn)

        addView(actionRow, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun addLetterRow(letters: String, keyHeight: Int, keyMargin: Int) {
        val row = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER
        }
        for (ch in letters) {
            row.addView(createLetterKey(ch, keyHeight, keyMargin))
        }
        addView(row, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    private fun createLetterKey(letter: Char, height: Int, margin: Int): Button {
        return createKey(letter.toString(), (resources.displayMetrics.density * 34).toInt(), height, margin) {
            listener?.onLetter(letter)
        }.also { keyButtons[letter.lowercaseChar()] = it }
    }

    private fun createActionKey(
        label: String,
        width: Int,
        height: Int,
        margin: Int,
        onClick: () -> Unit
    ): Button = createKey(label, width, height, margin, onClick)

    private fun createKey(
        label: String,
        width: Int,
        height: Int,
        margin: Int,
        onClick: () -> Unit
    ): Button {
        val params = LayoutParams(width, height).apply {
            setMargins(margin, margin, margin, margin)
        }
        return Button(context).apply {
            text = label
            setTextSize(TypedValue.COMPLEX_UNIT_SP, if (label.length == 1) 16f else 12f)
            background = ContextCompat.getDrawable(context, R.drawable.keyboard_key_bg)
            setTextColor(ContextCompat.getColor(context, R.color.keyboard_key_text))
            isAllCaps = false
            setOnClickListener { onClick() }
            layoutParams = params
        }
    }
}
