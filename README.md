# Word Game (Android)

A Wordle-style word guessing game for **English** and **French**, with word lengths of 4, 5, 6, or 7 letters.

## Features

- 6 attempts on a letter grid (columns = word length, rows = 6)
- On-screen QWERTY (English) or AZERTY (French) keyboard
- Letter feedback colors on the grid and keyboard (correct / present / absent)
- Navigation drawer (burger menu) with **Settings** (language & word length) and **New game**
- Separate word lists per language in `app/src/main/assets/words/`

## Requirements

- Android Studio Ladybug (2024.2) or newer recommended
- JDK 17
- Android SDK 35

## Build & run

1. Open this folder in Android Studio.
2. Let Gradle sync (Android Studio will create the wrapper if needed).
3. Run on an emulator or device (API 26+).

From the command line (after the Gradle wrapper exists):

```bash
./gradlew assembleDebug
```

## Word lists

| File pattern | Source |
|--------------|--------|
| `en_5.txt` | [tabatkins/wordle-list](https://github.com/tabatkins/wordle-list) (curated 5-letter guesses) |
| `en_4.txt`, `en_6.txt`, `en_7.txt` | [dwyl/english-words](https://github.com/dwyl/english-words) filtered by [Hermit Dave 50k frequency list](https://github.com/hermitdave/FrequencyWords) |
| `fr_*.txt` | [Taknok/French-Wordlist](https://github.com/Taknok/French-Wordlist) + [51413resu/full-list-of-french-words](https://github.com/51413resu/full-list-of-french-words), accents normalized to a–z |

Counts (unique words, letters a–z only):

| Length | English | French |
|--------|---------|--------|
| 4 | 3,031 | 1,960 |
| 5 | 14,855 | 6,174 |
| 6 | 5,857 | 14,291 |
| 7 | 6,083 | 25,888 |

To regenerate lists from sources, download the source `.txt` files to the project root and run the filter script documented in `scripts/filter_words.ps1`.

## Project structure

- `app/src/main/java/com/wordgame/app/` — game logic and UI
- `app/src/main/assets/words/` — bundled dictionaries
- `app/src/main/res/values-fr/` — French UI strings
