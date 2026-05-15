$ip = "10.0.0.254:45749"
Write-Host "Conectando a ADB inalambrico en $ip..." -ForegroundColor Cyan
adb disconnect
adb connect $ip
adb devices
