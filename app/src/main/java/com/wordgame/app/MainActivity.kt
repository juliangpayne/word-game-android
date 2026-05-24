package com.wordgame.app

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.GravityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wordgame.app.data.GameLanguage
import com.wordgame.app.data.GamePreferences
import com.wordgame.app.data.WordRepository
import com.wordgame.app.databinding.ActivityMainBinding
import com.wordgame.app.databinding.DialogSettingsBinding
import com.wordgame.app.game.GameController
import com.wordgame.app.game.LetterResult
import com.wordgame.app.ui.GameKeyboardView
import java.util.Locale

class MainActivity : AppCompatActivity(), GameKeyboardView.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: GamePreferences
    private lateinit var wordRepository: WordRepository
    private var gameController: GameController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupSystemBarInsets()

        preferences = GamePreferences(this)
        wordRepository = WordRepository(this)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_settings -> showSettingsDialog()
                R.id.nav_new_game -> startNewGame()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.gameKeyboard.setListener(this)
        setupHintPane()
        startNewGame()
    }

    private fun startNewGame() {
        applyLocale(preferences.language)
        val controller = GameController(
            wordRepository,
            preferences.language,
            preferences.wordLength,
            preferences.showFirstLetterHint,
            preferences.uniqueLettersOnly
        ).also { gameController = it }
        controller.newGame()
        binding.gameKeyboard.setLanguage(preferences.language)
        syncHintPaneSwitches()
        refreshUi(controller.getState(), clearMessage = true)
    }

    private fun refreshUi(state: com.wordgame.app.game.GameState, clearMessage: Boolean = false) {
        binding.gameGrid.render(state)
        updateHintPane(state)
        if (state.isComplete) {
            binding.gameKeyboard.resetKeyStates()
        } else {
            binding.gameKeyboard.updateKeyStates(computeKeyboardStates(state))
        }

        if (clearMessage) {
            binding.messageText.visibility = View.GONE
        }

        when {
            state.isWon -> showMessage(getString(R.string.you_won))
            state.isLost -> showMessage(
                getString(R.string.the_word_was, state.targetWord.uppercase())
            )
        }

        binding.gameKeyboard.isEnabled = !state.isComplete
    }

    private fun computeKeyboardStates(
        state: com.wordgame.app.game.GameState
    ): Map<Char, LetterResult> {
        val best = mutableMapOf<Char, LetterResult>()
        val priority = mapOf(
            LetterResult.CORRECT to 3,
            LetterResult.PRESENT to 2,
            LetterResult.ABSENT to 1
        )
        state.guesses.forEachIndexed { rowIndex, guess ->
            val row = state.results[rowIndex]
            for (i in row.indices) {
                val letter = guess[i].lowercaseChar()
                val current = best[letter]
                if (current == null || priority[row[i]]!! > priority[current]!!) {
                    best[letter] = row[i]
                }
            }
        }
        return best
    }

    private fun setupHintPane() {
        syncHintPaneSwitches()
    }

    private fun syncHintPaneSwitches() {
        binding.showFirstLetterSwitch.setOnCheckedChangeListener(null)
        binding.showSolutionSwitch.setOnCheckedChangeListener(null)
        binding.showFirstLetterSwitch.isChecked = preferences.showFirstLetterHint
        binding.showSolutionSwitch.isChecked = preferences.showSolution
        setupHintPaneSwitchListeners()
    }

    private fun setupHintPaneSwitchListeners() {
        binding.showFirstLetterSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.showFirstLetterHint = checked
            val state = gameController?.setShowFirstLetterHint(checked) ?: return@setOnCheckedChangeListener
            refreshUi(state)
        }
        binding.showSolutionSwitch.setOnCheckedChangeListener { _, checked ->
            preferences.showSolution = checked
            gameController?.getState()?.let { refreshUi(it) }
        }
    }

    private fun updateHintPane(state: com.wordgame.app.game.GameState) {
        if (binding.showFirstLetterSwitch.isChecked) {
            binding.firstLetterHintText.text = getString(
                R.string.hint_first_letter_format,
                state.targetWord.first().uppercaseChar()
            )
            binding.firstLetterHintText.visibility = View.VISIBLE
        } else {
            binding.firstLetterHintText.visibility = View.GONE
        }

        if (binding.showSolutionSwitch.isChecked) {
            binding.solutionHintText.text = getString(
                R.string.hint_solution_format,
                state.targetWord.uppercase()
            )
            binding.solutionHintText.visibility = View.VISIBLE
        } else {
            binding.solutionHintText.visibility = View.GONE
        }
    }

    private fun showMessage(text: String) {
        binding.messageText.text = text
        binding.messageText.visibility = View.VISIBLE
    }

    override fun onLetter(letter: Char) {
        val controller = gameController ?: return
        controller.typeLetter(letter)
        refreshUi(controller.getState())
    }

    override fun onBackspace() {
        val controller = gameController ?: return
        controller.deleteLetter()
        refreshUi(controller.getState())
    }

    override fun onEnter() {
        val controller = gameController ?: return
        val (state, result) = controller.submitGuess()
        when (result) {
            GameController.SubmitResult.NotInList -> {
                Toast.makeText(this, R.string.not_in_word_list, Toast.LENGTH_SHORT).show()
                refreshUi(state)
            }
            GameController.SubmitResult.Success -> refreshUi(state, clearMessage = true)
            GameController.SubmitResult.AlreadyComplete -> Unit
        }
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(layoutInflater)

        when (preferences.language) {
            GameLanguage.ENGLISH -> dialogBinding.langEnglish.isChecked = true
            GameLanguage.FRENCH -> dialogBinding.langFrench.isChecked = true
        }

        val lengthButtons = listOf(
            dialogBinding.length4 to 4,
            dialogBinding.length5 to 5,
            dialogBinding.length6 to 6,
            dialogBinding.length7 to 7
        )
        lengthButtons.forEach { (button, len) ->
            button.text = getString(R.string.word_length_format, len)
            button.isChecked = preferences.wordLength == len
        }

        dialogBinding.uniqueLettersSwitch.isChecked = preferences.uniqueLettersOnly

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.apply_and_new_game) { _, _ ->
                preferences.language = when (dialogBinding.languageGroup.checkedRadioButtonId) {
                    R.id.lang_french -> GameLanguage.FRENCH
                    else -> GameLanguage.ENGLISH
                }
                preferences.wordLength = lengthButtons
                    .first { (btn, _) -> btn.isChecked }
                    .second
                preferences.uniqueLettersOnly = dialogBinding.uniqueLettersSwitch.isChecked
                startNewGame()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun setupSystemBarInsets() {
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { container, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            container.updatePadding(top = statusBars.top)
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.gameKeyboard) { keyboard, insets ->
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            keyboard.updatePadding(bottom = navBars.bottom)
            insets
        }
    }

    private fun applyLocale(language: GameLanguage) {
        val locale = when (language) {
            GameLanguage.ENGLISH -> Locale.ENGLISH
            GameLanguage.FRENCH -> Locale.FRENCH
        }
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        binding.toolbar.title = getString(R.string.app_name)
        binding.navigationView.menu.findItem(R.id.nav_settings)?.title =
            getString(R.string.menu_settings)
        binding.navigationView.menu.findItem(R.id.nav_new_game)?.title =
            getString(R.string.new_game)
    }
}
