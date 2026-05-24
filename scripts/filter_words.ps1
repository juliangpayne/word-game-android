# Regenerate app/src/main/assets/words from source files in project root.
# English: Wordle list (5-letter) + dwyl intersected with Hermit Dave 50k frequency list (4/6/7).
# French: Taknok + 51413resu lists, accents normalized.

param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

Set-Location $ProjectRoot

function Remove-Diacritics([string]$text) {
    $normalized = $text.Normalize([Text.NormalizationForm]::FormD)
    $sb = New-Object System.Text.StringBuilder
    foreach ($c in $normalized.ToCharArray()) {
        if ([Globalization.CharUnicodeInfo]::GetUnicodeCategory($c) -ne 'NonSpacingMark') {
            [void]$sb.Append($c)
        }
    }
    return $sb.ToString().Normalize([Text.NormalizationForm]::FormC)
}

function Ensure-File([string]$path, [string]$url) {
    if (-not (Test-Path $path)) {
        Write-Host "Downloading $url ..."
        Invoke-WebRequest -Uri $url -OutFile $path
    }
}

$outDir = "app\src\main\assets\words"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Ensure-File "words_en_source.txt" "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt"
Ensure-File "wordle_en_5.txt" "https://raw.githubusercontent.com/tabatkins/wordle-list/main/words"
Ensure-File "en_50k.txt" "https://raw.githubusercontent.com/hermitdave/FrequencyWords/master/content/2018/en/en_50k.txt"

$common = @{}
Get-Content "en_50k.txt" | ForEach-Object {
    $word = ($_ -split '\s+')[0]
    if ($word) { $common[$word.ToLower()] = $true }
}
Write-Host "English frequency words: $($common.Keys.Count)"

$w5 = Get-Content "wordle_en_5.txt" | ForEach-Object { $_.Trim().ToLower() } |
    Where-Object { $_ -match '^[a-z]{5}$' } | Sort-Object -Unique
$w5 | Set-Content (Join-Path $outDir "en_5.txt") -Encoding UTF8
Write-Host "en_5 (Wordle): $($w5.Count)"

foreach ($len in 4, 6, 7) {
    $words = Get-Content "words_en_source.txt" | ForEach-Object { $_.Trim().ToLower() } |
        Where-Object { $_ -match "^[a-z]{$len}$" -and $common.ContainsKey($_) } |
        Sort-Object -Unique
    $words | Set-Content (Join-Path $outDir "en_${len}.txt") -Encoding UTF8
    Write-Host "en_${len} (frequency filtered): $($words.Count)"
}

Ensure-File "words_fr_source.txt" "https://raw.githubusercontent.com/Taknok/French-Wordlist/master/francais.txt"
Ensure-File "words_fr_big.txt" "https://raw.githubusercontent.com/51413resu/full-list-of-french-words/master/fr.txt"

$frWords = @{}
foreach ($src in @("words_fr_source.txt", "words_fr_big.txt")) {
    Get-Content $src | ForEach-Object {
        $w = (Remove-Diacritics $_.Trim().ToLower()) -replace "[^a-z]", ""
        if ($w.Length -ge 4 -and $w.Length -le 7) { $frWords[$w] = $true }
    }
}
foreach ($len in 4..7) {
    $words = $frWords.Keys | Where-Object { $_.Length -eq $len } | Sort-Object
    $words | Set-Content (Join-Path $outDir "fr_${len}.txt") -Encoding UTF8
    Write-Host "fr_${len}: $($words.Count)"
}
