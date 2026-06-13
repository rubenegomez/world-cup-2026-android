# Wrapper para ejecutar el scraper de resultados en Python
# Evita la duplicación de lógica de parseo en PowerShell y Python.

$scriptPath = Join-Path $PSScriptRoot "scraper.py"
Write-Host "Ejecutando actualizador de resultados en Python desde: $scriptPath..." -ForegroundColor Cyan

if (Get-Command python -ErrorAction SilentlyContinue) {
    python $scriptPath
} else {
    Write-Error "No se encontró el comando 'python' en el sistema. Por favor instala Python o agrégalo a tu variable de entorno PATH."
}
