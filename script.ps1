
$content = Get-Content -Path app\src\main\java\com\example\worldcup2026\ui\FixtureScreen.kt -Raw
$prodeStart = $content.IndexOf("            // SECCIÓN PRODE (Predicción)")
$tandaPenalesStart = $content.IndexOf("            val isLivePenalties")
$tandaPenalesEnd = $content.IndexOf("            // Referencia sutil del Estadio")

if ($prodeStart -ne -1 -and $tandaPenalesStart -ne -1 -and $tandaPenalesEnd -ne -1) {
    $part1 = $content.Substring(0, $prodeStart)
    $prodePart = $content.Substring($prodeStart, $tandaPenalesStart - $prodeStart)
    $tandaPart = $content.Substring($tandaPenalesStart, $tandaPenalesEnd - $tandaPenalesStart)
    $part3 = $content.Substring($tandaPenalesEnd)

    $newContent = $part1 + $tandaPart + $prodePart + $part3
    [System.IO.File]::WriteAllText("app\src\main\java\com\example\worldcup2026\ui\FixtureScreen.kt", $newContent, [System.Text.Encoding]::UTF8)
    Write-Output "Reorder successful!"
} else {
    Write-Output "Failed to find sections."
}

