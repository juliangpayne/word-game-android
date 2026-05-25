package com.wordgame.app.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.wordgame.app.R
import com.wordgame.app.game.GameState
import com.wordgame.app.game.LetterResult
import com.wordgame.app.game.WordEvaluator

class GameGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    private var gridColumns: Int = 5
    private val cells = mutableListOf<TextView>()

    init {
        rowCount = WordEvaluator.MAX_ATTEMPTS
        useDefaultMargins = false
    }

    fun configure(wordLength: Int) {
        val neededCells = WordEvaluator.MAX_ATTEMPTS * wordLength
        if (gridColumns == wordLength && cells.size == neededCells) return

        removeAllViews()
        cells.clear()

        gridColumns = wordLength
        columnCount = wordLength
        rowCount = WordEvaluator.MAX_ATTEMPTS

        val cellSize = resources.displayMetrics.density * 52f
        val margin = (resources.displayMetrics.density * 4).toInt()

        for (row in 0 until WordEvaluator.MAX_ATTEMPTS) {
            for (col in 0 until wordLength) {
                val cell = TextView(context).apply {
                    gravity = Gravity.CENTER
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                    typeface = Typeface.DEFAULT_BOLD
                    background = ContextCompat.getDrawable(context, R.drawable.cell_border)
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
                val params = LayoutParams().apply {
                    width = cellSize.toInt()
                    height = cellSize.toInt()
                    rowSpec = spec(row)
                    columnSpec = spec(col)
                    setMargins(margin, margin, margin, margin)
                }
                addView(cell, params)
                cells.add(cell)
            }
        }
    }

    fun render(state: GameState) {
        configure(state.wordLength)
        val columns = state.wordLength
        for (row in 0 until WordEvaluator.MAX_ATTEMPTS) {
            for (col in 0 until columns) {
                val index = row * columns + col
                if (index >= cells.size) continue
                val cell = cells[index]
                val letter: Char?
                val result: LetterResult

                when {
                    row < state.guesses.size -> {
                        val guess = state.guesses[row]
                        if (col >= guess.length) {
                            letter = null
                            result = LetterResult.EMPTY
                        } else {
                            letter = guess[col]
                            result = state.results[row][col]
                        }
                    }
                    row == state.currentRow && col < state.currentGuess.length -> {
                        letter = state.currentGuess[col]
                        result = if (state.showFirstLetterHint && col == 0) {
                            LetterResult.HINT
                        } else {
                            LetterResult.TYPED
                        }
                    }
                    else -> {
                        letter = null
                        result = LetterResult.EMPTY
                    }
                }

                cell.text = letter?.uppercaseChar()?.toString() ?: ""
                applyCellStyle(cell, result)
            }
        }
    }

    private fun applyCellStyle(cell: TextView, result: LetterResult) {
        val (bg, fg) = when (result) {
            LetterResult.CORRECT -> R.color.letter_correct to R.color.white
            LetterResult.PRESENT -> R.color.letter_present to R.color.white
            LetterResult.ABSENT -> R.color.letter_absent to R.color.white
            LetterResult.HINT -> R.color.letter_hint to R.color.black
            LetterResult.TYPED, LetterResult.EMPTY -> R.color.grid_empty to R.color.black
        }
        cell.setTextColor(ContextCompat.getColor(context, fg))
        when (result) {
            LetterResult.TYPED, LetterResult.EMPTY -> {
                cell.background = ContextCompat.getDrawable(context, R.drawable.cell_border)
            }
            else -> {
                cell.background = null
                cell.setBackgroundColor(ContextCompat.getColor(context, bg))
            }
        }
    }
}
